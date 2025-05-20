package com.example.gatoshunter

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.clases.Gato
import com.example.gatoshunter.adaptes.GatoAdapter
import com.example.gatoshunter.clases.User
import com.example.miapp.database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.text.split
import kotlin.text.toMutableList

class BuscarGato : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: GatoAdapter
    private lateinit var timerTextView: TextView
    private lateinit var temporizadorMedianoche: TemporizadorMedianoche

    private var currentDailyCatsList: List<Gato> = emptyList()
    private var currentUser: User? = null

    // Keys SharedPreferences
    private val KEY_DAILY_CAT_AVAILABILITY_MAP_BASE = "daily_cat_availability_map" // Base key
    private val KEY_LAST_GENERATION_TIMESTAMP_BASE = "last_generation_timestamp_buscar_gato" // Base key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buscar_gatos)

        val backButton: Button = findViewById(R.id.backbutton)
        val buyButton: Button = findViewById(R.id.buybutton)
        timerTextView = findViewById(R.id.temporizador)

        dbHelper = DatabaseHelper(this)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter = GatoAdapter(emptyList(), null)
        recyclerView.adapter = adapter

        // Obtener al usuario actual
        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = applicationContext.getAppSharedPreferences()
            currentUser = prefs.getUserAsync("Usuario")

            if (currentUser != null) {
                currentDailyCatsList = loadOrCreateDailyCats(currentUser!!)
                withContext(Dispatchers.Main) {
                    adapter.actualizarLista(currentDailyCatsList)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BuscarGato, "Error: No se pudo cargar el usuario.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        buyButton.setOnClickListener {
            resolverAdoptacion()
        }

        temporizadorMedianoche = TemporizadorMedianoche(timerTextView) {
            lifecycleScope.launch(Dispatchers.IO) {
                currentUser?.let { user ->
                    updateRecyclerViewData(user)
                } ?: run {
                    Log.e("BuscarGato", "Temporizador de medianoche activado sin usuario.")
                }
            }
        }
        temporizadorMedianoche.iniciar()
    }

    // --- Gatos diarios ---

    // Cargar o seleccionar gatos basado en el usuario.
    private fun loadOrCreateDailyCats(user: User): List<Gato> {
        val lastTimestamp = cargarUltimaGeneracionTimestamp(user.id!!)
        val currentCalendar = Calendar.getInstance()
        val lastGenerationCalendar = Calendar.getInstance().apply { timeInMillis = lastTimestamp }

        val isSameDay = lastTimestamp != 0L &&
                currentCalendar.get(Calendar.YEAR) == lastGenerationCalendar.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.DAY_OF_YEAR) == lastGenerationCalendar.get(Calendar.DAY_OF_YEAR)

        return if (isSameDay) {
            val catAvailabilityMap = cargarDailyCatAvailabilityMap(user.id!!)
            val allCats = dbHelper.obtenerGatos()

            val currentUserCats = dbHelper.obtenerGatosByUser(user)

            allCats.filter { gato ->
                val isAvailable = catAvailabilityMap[gato.id.toString()] == true
                val isOwnedByUser = currentUserCats.any { it.id == gato.id }
                isAvailable && !isOwnedByUser
            }.also {
                Log.d("BuscarGato", "Gatos cargados para el día del usuario ${user.id}: ${it.size}")
            }
        } else {
            Log.d("BuscarGato", "Nuevo día o no timestamp para usuario ${user.id}. Generando nuevos gatos y actualizando SharedPreferences.")
            selectAndSaveNewDailyCats(user)
        }
    }

    // Se eligen 5 gatos aleatorios, y se añaden a cada usuario especifico
    private fun selectAndSaveNewDailyCats(user: User): List<Gato> {
        val allPotentialCats = dbHelper.obtenerGatos()
        val selectedCats = allPotentialCats.shuffled().take(5)

        //Se guarda el id de cada gato seleccionado con el valor True
        val catAvailabilityMap = mutableMapOf<String, Boolean>()
        selectedCats.forEach { gato ->
            gato.id?.let { catAvailabilityMap[it.toString()] = true }
        }

        // Se guarda el mapa de disponibilidad de gatos
        guardarDailyCatAvailabilityMap(user.id!!, catAvailabilityMap)

        // Se guarda cuando se hizo este proceso
        guardarUltimaGeneracionTimestamp(System.currentTimeMillis(), user.id!!)

        Log.d("BuscarGato", "Generados y guardados ${selectedCats.size} gatos diarios para usuario ${user.id}.")
        return selectedCats
    }


    // --- SharedPreferences User Logic ---

    // guarda el mapa de disponibilidad de gatos por Usuario
    private fun guardarDailyCatAvailabilityMap(userId: Int, map: Map<String, Boolean>) {
        val prefs = getSharedPreferences(KEY_DAILY_CAT_AVAILABILITY_MAP_BASE, MODE_PRIVATE)
        val key = "${KEY_DAILY_CAT_AVAILABILITY_MAP_BASE}_$userId"
        val jsonObject = JSONObject(map as Map<*, *>).toString()
        prefs.edit().putString(key, jsonObject).apply()
        Log.d("BuscarGato", "Guardado mapa GatoId->Availability para usuario $userId: $jsonObject")
    }

    // Garga los gatos disponibles para el usuario
    private fun cargarDailyCatAvailabilityMap(userId: Int): Map<String, Boolean> {
        val prefs = getSharedPreferences(KEY_DAILY_CAT_AVAILABILITY_MAP_BASE, MODE_PRIVATE)
        val key = "${KEY_DAILY_CAT_AVAILABILITY_MAP_BASE}_$userId"
        val jsonString = prefs.getString(key, null)

        return if (jsonString.isNullOrEmpty()) {
            emptyMap()
        } else {
            try {
                val jsonObject = JSONObject(jsonString)
                val map = mutableMapOf<String, Boolean>()
                jsonObject.keys().forEach { jsonKey ->
                    map[jsonKey] = jsonObject.getBoolean(jsonKey)
                }
                Log.d("BuscarGato", "Cargado mapa GatoId->Availability para usuario $userId: $map")
                map
            } catch (e: Exception) {
                Log.e("BuscarGato", "Error al parsear el mapa GatoId->Availability desde SharedPreferences para usuario $userId", e)
                emptyMap()
            }
        }
    }

    // --- SharedPreferences Time Logic  ---

    // Guarda cuando se hizo la lista, por usuario.
    private fun guardarUltimaGeneracionTimestamp(timestamp: Long, userId: Int) {
        val prefs = getSharedPreferences(KEY_LAST_GENERATION_TIMESTAMP_BASE, MODE_PRIVATE)
        val key = "${KEY_LAST_GENERATION_TIMESTAMP_BASE}_$userId"
        prefs.edit().putLong(key, timestamp).apply()
        Log.d("BuscarGato", "Timestamp de última generación guardado para usuario $userId: $timestamp")
    }

    // Cargar cuando se hizo la lista, por usuario.
    private fun cargarUltimaGeneracionTimestamp(userId: Int): Long {
        val prefs = getSharedPreferences(KEY_LAST_GENERATION_TIMESTAMP_BASE, MODE_PRIVATE)
        val key = "${KEY_LAST_GENERATION_TIMESTAMP_BASE}_$userId" // User-specific key
        val timestamp = prefs.getLong(key, 0L)
        Log.d("BuscarGato", "Timestamp de última generación cargado para usuario $userId: $timestamp")
        return timestamp
    }

    // Resetear los gatos
    private suspend fun updateRecyclerViewData(user: User) {
        val nuevosGatos = selectAndSaveNewDailyCats(user) // Pass the user to get new cats for them
        withContext(Dispatchers.Main) {
            currentDailyCatsList = nuevosGatos
            Toast.makeText(this@BuscarGato, "¡Medianoche alcanzada! Recargando gatos...", Toast.LENGTH_SHORT).show()
            adapter.actualizarLista(currentDailyCatsList)
            adapter.selectedItemId = null
        }
    }

    // --- Adoptacion ---

    private fun resolverAdoptacion() {
        val gatoSeleccionado = adapter.getGatoSeleccionado()
        if (gatoSeleccionado != null && currentUser != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                dbHelper.insertarGatoUser(gatoSeleccionado, currentUser!!)

                // Actualizar la disponibilidad del gato
                val currentAvailabilityMap = cargarDailyCatAvailabilityMap(currentUser!!.id!!).toMutableMap()
                gatoSeleccionado.id?.let {
                    currentAvailabilityMap[it.toString()] = false // Se marca que no esta dispnible
                }
                guardarDailyCatAvailabilityMap(currentUser!!.id!!, currentAvailabilityMap)

                withContext(Dispatchers.Main) {
                    currentDailyCatsList = currentDailyCatsList.filter { it.id != gatoSeleccionado.id }
                    adapter.actualizarLista(currentDailyCatsList)
                    adapter.selectedItemId = null

                    mostrarDialogoExito()
                }
            }
        } else {
            val message = if (gatoSeleccionado == null)
                "Selecciona un gato primero"
            else
                "Error: No se pudo cargar el usuario."
            Toast.makeText(this@BuscarGato, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoExito() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_compra_exitosa, null)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnAceptar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
