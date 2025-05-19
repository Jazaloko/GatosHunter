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
    private var currentUser: User? = null // Variable to store the current user

    // Keys for SharedPreferences, now *user-specific*
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

        // Obtain the current user when the activity starts
        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = applicationContext.getAppSharedPreferences()
            currentUser = prefs.getUserAsync("Usuario") // Get the user (should now work with extensions)

            if (currentUser != null) {
                // Load or create daily cats for the current user
                currentDailyCatsList = loadOrCreateDailyCats(currentUser!!)
                withContext(Dispatchers.Main) {
                    adapter.actualizarLista(currentDailyCatsList)
                }
            } else {
                // Handle the case where the user could not be loaded
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BuscarGato, "Error: No se pudo cargar el usuario.", Toast.LENGTH_LONG).show()
                    finish() // Close the activity if no user
                }
            }
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        buyButton.setOnClickListener {
            resolverCompra()
        }

        temporizadorMedianoche = TemporizadorMedianoche(timerTextView) {
            lifecycleScope.launch(Dispatchers.IO) {
                // Pass the current user to reset cats for that specific user
                currentUser?.let { user ->
                    updateRecyclerViewData(user)
                } ?: run {
                    Log.e("BuscarGato", "Temporizador de medianoche activado sin usuario.")
                }
            }
        }
        temporizadorMedianoche.iniciar()
    }

    // --- Daily Cat Management Logic (User-specific) ---

    // Load saved daily cat IDs or select new ones based on the day and user.
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

            // Filter by availability and ensure the user doesn't already own it
            val currentUserCats = dbHelper.obtenerGatosByUser(user) // Use the passed user

            allCats.filter { gato ->
                val isAvailable = catAvailabilityMap[gato.id.toString()] == true
                val isOwnedByUser = currentUserCats.any { it.id == gato.id }
                isAvailable && !isOwnedByUser
            }.also {
                Log.d("BuscarGato", "Gatos cargados para el día del usuario ${user.id}: ${it.size}")
            }
        } else {
            Log.d("BuscarGato", "Nuevo día o no timestamp para usuario ${user.id}. Generando nuevos gatos y actualizando SharedPreferences.")
            selectAndSaveNewDailyCats(user) // Pass the user
        }
    }

    // Selects 5 random cats, marks them as available, and saves their IDs in SharedPreferences (user-specific).
    private fun selectAndSaveNewDailyCats(user: User): List<Gato> {
        val allPotentialCats = dbHelper.obtenerGatos()
        val selectedCats = allPotentialCats.shuffled().take(5)

        val catAvailabilityMap = mutableMapOf<String, Boolean>()
        selectedCats.forEach { gato ->
            gato.id?.let { catAvailabilityMap[it.toString()] = true }
        }

        // Save the map of Gato ID -> availability status for this user
        guardarDailyCatAvailabilityMap(user.id!!, catAvailabilityMap)

        // Save the timestamp of generation for this user
        guardarUltimaGeneracionTimestamp(System.currentTimeMillis(), user.id!!)

        Log.d("BuscarGato", "Generados y guardados ${selectedCats.size} gatos diarios para usuario ${user.id}.")
        return selectedCats
    }

    // --- SharedPreferences Logic (for Gato ID -> Availability Map, per user) ---

    // Saves the map of Gato ID -> availability status in SharedPreferences for a user.
    private fun guardarDailyCatAvailabilityMap(userId: Int, map: Map<String, Boolean>) {
        val prefs = getSharedPreferences(KEY_DAILY_CAT_AVAILABILITY_MAP_BASE, MODE_PRIVATE)
        val key = "${KEY_DAILY_CAT_AVAILABILITY_MAP_BASE}_$userId" // User-specific key
        val jsonObject = JSONObject(map as Map<*, *>).toString()
        prefs.edit().putString(key, jsonObject).apply()
        Log.d("BuscarGato", "Guardado mapa GatoId->Availability para usuario $userId: $jsonObject")
    }

    // Loads the map of Gato ID -> availability status from SharedPreferences for a user.
    private fun cargarDailyCatAvailabilityMap(userId: Int): Map<String, Boolean> {
        val prefs = getSharedPreferences(KEY_DAILY_CAT_AVAILABILITY_MAP_BASE, MODE_PRIVATE)
        val key = "${KEY_DAILY_CAT_AVAILABILITY_MAP_BASE}_$userId" // User-specific key
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

    // --- SharedPreferences Logic (for Timestamp, per user) ---

    // Save the timestamp of the last generation of cats (per user).
    private fun guardarUltimaGeneracionTimestamp(timestamp: Long, userId: Int) {
        val prefs = getSharedPreferences(KEY_LAST_GENERATION_TIMESTAMP_BASE, MODE_PRIVATE)
        val key = "${KEY_LAST_GENERATION_TIMESTAMP_BASE}_$userId" // User-specific key
        prefs.edit().putLong(key, timestamp).apply()
        Log.d("BuscarGato", "Timestamp de última generación guardado para usuario $userId: $timestamp")
    }

    // Load the timestamp of the last generation of cats (per user).
    private fun cargarUltimaGeneracionTimestamp(userId: Int): Long {
        val prefs = getSharedPreferences(KEY_LAST_GENERATION_TIMESTAMP_BASE, MODE_PRIVATE)
        val key = "${KEY_LAST_GENERATION_TIMESTAMP_BASE}_$userId" // User-specific key
        val timestamp = prefs.getLong(key, 0L)
        Log.d("BuscarGato", "Timestamp de última generación cargado para usuario $userId: $timestamp")
        return timestamp
    }

    // Reset cats at midnight, similar to how VenderGato resets buyers.
    private suspend fun updateRecyclerViewData(user: User) {
        val nuevosGatos = selectAndSaveNewDailyCats(user) // Pass the user to get new cats for them
        withContext(Dispatchers.Main) {
            currentDailyCatsList = nuevosGatos
            Toast.makeText(this@BuscarGato, "¡Medianoche alcanzada! Recargando gatos...", Toast.LENGTH_SHORT).show()
            adapter.actualizarLista(currentDailyCatsList)
            adapter.selectedItemId = null
        }
    }

    // --- Purchase Logic ---

    private fun resolverCompra() {
        val gatoSeleccionado = adapter.getGatoSeleccionado()
        if (gatoSeleccionado != null && currentUser != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                dbHelper.insertarGatoUser(gatoSeleccionado, currentUser!!) // Use currentUser to insert

                // Update the availability map in SharedPreferences for this user
                val currentAvailabilityMap = cargarDailyCatAvailabilityMap(currentUser!!.id!!).toMutableMap()
                gatoSeleccionado.id?.let {
                    currentAvailabilityMap[it.toString()] = false // Mark as unavailable for this user
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
