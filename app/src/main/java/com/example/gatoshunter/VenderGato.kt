package com.example.gatoshunter

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.clases.Comprador
import com.example.gatoshunter.adaptes.CompradorAdapter
import com.example.gatoshunter.clases.Gato
import com.example.gatoshunter.clases.User
import com.example.miapp.database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import org.json.JSONObject // Necesitaremos alguna forma de manejar la asociación CompradorId -> GatoName

class VenderGato : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var adapter: CompradorAdapter
    private lateinit var timerTextView: TextView
    private lateinit var temporizadorMedianoche: TemporizadorMedianoche

    private var currentDailyBuyersList: List<CompradorConGato> = emptyList()
    private var currentUser: User? = null // Variable para almacenar el usuario actual
    // Clave para guardar la asociación CompradorId -> NombreGato interesado para la lista diaria (por usuario)
    private val KEY_DAILY_BUYER_CAT_MAP = "daily_buyer_cat_map"
    // Clave base para guardar el timestamp de la última generación (per user)
    private val KEY_LAST_GENERATION_TIMESTAMP_BASE = "last_generation_timestamp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vender_gatos)

        val backButton: Button = findViewById(R.id.backbutton)
        val sellButton: Button = findViewById(R.id.sellbutton)
        timerTextView = findViewById(R.id.temporizador)

        dbHelper = DatabaseHelper(this)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        adapter = CompradorAdapter(emptyList()) { compradorConGato ->
            intentarVenderDesdeClick(compradorConGato)
        }
        recyclerView.adapter = adapter

        // Obtener el usuario actual al iniciar la actividad
        lifecycleScope.launch(Dispatchers.IO) {
            // Asumiendo que getAppSharedPreferences() y getUserAsync() existen
            val prefs = applicationContext.getAppSharedPreferences()
            currentUser = prefs.getUserAsync("Usuario")

            if (currentUser != null) {
                currentDailyBuyersList = loadOrCreateDailyBuyers(currentUser!!)
                withContext(Dispatchers.Main) {
                    adapter.actualizarLista(currentDailyBuyersList)
                }
            } else {
                // Manejar el caso donde no se pudo obtener el usuario
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@VenderGato, "Error: No se pudo cargar el usuario.", Toast.LENGTH_LONG).show()
                    finish() // Cerrar la actividad si no hay usuario
                }
            }
        }


        //Botones
        backButton.setOnClickListener {
            startActivity(Intent(this@VenderGato, MainActivity::class.java))
            finish()
        }

        sellButton.setOnClickListener {
            resolverVenta()
        }

        // Timer
        temporizadorMedianoche = TemporizadorMedianoche(timerTextView) {
            lifecycleScope.launch(Dispatchers.IO) {
                // Pasar el usuario actual para resetear los compradores vendidos de ese usuario
                currentUser?.let { user ->
                    updateRecyclerViewData(user)
                } ?: run {
                    // Manejar caso sin usuario si el timer se dispara antes
                    Log.e("VenderGato", "Temporizador de medianoche activado sin usuario.")
                }
            }
        }
        temporizadorMedianoche.iniciar()
    }

    data class CompradorConGato(val comprador: Comprador, val nombreGatoInteres: String?, var gatoUsuarioIdPreferido: Int? = null)

    //Cargar compradores diarios o crear si es un nuevo día. Ahora requiere el usuario y usa la DB.
    private fun loadOrCreateDailyBuyers(user: User): List<CompradorConGato> {
        // Pass the user's ID here!
        val lastTimestamp = cargarUltimaGeneracionTimestamp(user.id!!)
        val currentCalendar = Calendar.getInstance()
        val lastGenerationCalendar = Calendar.getInstance().apply { timeInMillis = lastTimestamp }

        val isSameDay = lastTimestamp != 0L &&
                currentCalendar.get(Calendar.YEAR) == lastGenerationCalendar.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.DAY_OF_YEAR) == lastGenerationCalendar.get(Calendar.DAY_OF_YEAR)

        return if (isSameDay) {
            val buyerCatMap = cargarDailyBuyerCatMap(user.id!!)
            val compradoresDelDiaDB = dbHelper.obtenerCompradoresDiariosByUser(user)
            val gatosUsuario = dbHelper.obtenerGatosByUser(user)

            compradoresDelDiaDB.mapNotNull { comprador ->
                val catName = buyerCatMap[comprador.id.toString()]
                if (catName != null) {
                    val gatoUsuarioCoincidente = gatosUsuario.find { it.nombre == catName }
                    CompradorConGato(comprador, catName, gatoUsuarioCoincidente?.id)
                } else {
                    Log.w("VenderGato", "No se encontró nombre de gato para comprador ${comprador.id} en el mapa del día de usuario ${user.id}.")
                    null
                }
            }.also {
                Log.d("VenderGato", "Compradores cargados para usuario ${user.id}: ${it.size}")
            }
        } else {
            Log.d("VenderGato", "Nuevo día o no timestamp para usuario ${user.id}. Generando nuevos compradores y actualizando DB.")
            selectAndSaveNewDailyBuyers(user)
        }
    }


    // Selecciona 3 compradores aleatorios, los añade a CompradorUser para el usuario,
    // asigna gatos aleatorios y guarda la asociación en SharedPreferences.
    // También actualiza el timestamp.
    private fun selectAndSaveNewDailyBuyers(user: User): List<CompradorConGato> {
        // Vacía la tabla CompradorUser para este usuario al inicio de un nuevo día
        dbHelper.eliminarCompradoresDiariosDeUsuario(user.id!!)
        Log.d("VenderGato", "Tabla CompradorUser vaciada para usuario ${user.id} al inicio del día.")

        val allPotentialBuyers = dbHelper.obtenerCompradores() // Get ALL potential buyers from main DB table
        val allPotentialGatos = dbHelper.obtenerGatos() // Get ALL potential gatos from main DB table
        val gatosUsuario = dbHelper.obtenerGatosByUser(user) // Get ALL gatos from user's DB table

        // Ensure we don't select more buyers/gatos than available
        val numberOfItemsToSelect = minOf(3, allPotentialBuyers.size, allPotentialGatos.size)
        val selectedBuyers = allPotentialBuyers.shuffled().take(numberOfItemsToSelect)
        val selectedGatos = allPotentialGatos.shuffled().take(numberOfItemsToSelect)

        val compradoresConGato = mutableListOf<CompradorConGato>()
        val buyerCatMap = mutableMapOf<String, String>() // Mapa para guardar CompradorId -> NombreGato

        selectedBuyers.zip(selectedGatos) { comprador, gato ->
            val compradorConGato = CompradorConGato(comprador, gato.nombre)
            // Verificar si el usuario tiene un gato con el mismo nombre
            val gatoUsuarioCoincidente = gatosUsuario.find { it.nombre == gato.nombre }
            compradorConGato.gatoUsuarioIdPreferido = gatoUsuarioCoincidente?.id

            compradoresConGato.add(compradorConGato)
            dbHelper.insertarCompradorDiario(comprador.id!!, user.id)
            buyerCatMap[comprador.id.toString()] = gato.nombre
        }

        // Guarda el mapa de asociación CompradorId -> NombreGato en SharedPreferences para este usuario (already user-specific)
        guardarDailyBuyerCatMap(user.id, buyerCatMap)

        // Guarda el timestamp de la generación (PER USER)
        guardarUltimaGeneracionTimestamp(System.currentTimeMillis(), user.id) // Pass the user's ID here!

        Log.d("VenderGato", "Generados y guardados ${compradoresConGato.size} compradores diarios con gatos para usuario ${user.id}.")
        return compradoresConGato
    }


    // --- Lógica para guardar y cargar la asociación CompradorId -> NombreGato interesado (en SP, por usuario) ---

    // Guarda el mapa de asociación CompradorId -> NombreGato interesado en SharedPreferences para un usuario
    private fun guardarDailyBuyerCatMap(userId: Int, map: Map<String, String>) {
        val prefs = getSharedPreferences(KEY_DAILY_BUYER_CAT_MAP, MODE_PRIVATE)
        val key = "${KEY_DAILY_BUYER_CAT_MAP}_$userId" // Clave única por usuario
        val jsonObject = JSONObject(map as Map<*, *>).toString() // Convertir el mapa a JSON String
        prefs.edit().putString(key, jsonObject).apply()
        Log.d("VenderGato", "Guardado mapa CompradorId->NombreGato para usuario $userId: $jsonObject")
    }

    // Carga el mapa de asociación CompradorId -> NombreGato interesado desde SharedPreferences para un usuario
    private fun cargarDailyBuyerCatMap(userId: Int): Map<String, String> {
        val prefs = getSharedPreferences(KEY_DAILY_BUYER_CAT_MAP, MODE_PRIVATE)
        val key = "${KEY_DAILY_BUYER_CAT_MAP}_$userId" // Clave única por usuario
        val jsonString = prefs.getString(key, null)

        return if (jsonString.isNullOrEmpty()) {
            emptyMap()
        } else {
            try {
                val jsonObject = JSONObject(jsonString)
                val map = mutableMapOf<String, String>()
                jsonObject.keys().forEach { jsonKey ->
                    map[jsonKey] = jsonObject.getString(jsonKey)
                }
                Log.d("VenderGato", "Cargado mapa CompradorId->NombreGato para usuario $userId: $map")
                map
            } catch (e: Exception) {
                Log.e("VenderGato", "Error al parsear el mapa CompradorId->NombreGato desde SharedPreferences para usuario $userId", e)
                emptyMap()
            }
        }
    }

    // Elimina el mapa de asociación CompradorId -> NombreGato interesado de SharedPreferences para un usuario
    private fun limpiarDailyBuyerCatMap(userId: Int) {
        val prefs = getSharedPreferences(KEY_DAILY_BUYER_CAT_MAP, MODE_PRIVATE)
        val key = "${KEY_DAILY_BUYER_CAT_MAP}_$userId" // Clave única por usuario
        prefs.edit().remove(key).apply()
        Log.d("VenderGato", "Mapa CompradorId->NombreGato limpio para usuario $userId")
    }

    // --- Lógica para el timestamp (en SP, global) ---

    // Guardar el timestamp de la última generación de compradores (per user)
    private fun guardarUltimaGeneracionTimestamp(timestamp: Long, userId: Int) {
        val prefs = getSharedPreferences(KEY_LAST_GENERATION_TIMESTAMP_BASE, MODE_PRIVATE)
        val key = "${KEY_LAST_GENERATION_TIMESTAMP_BASE}_$userId" // User-specific key
        prefs.edit().putLong(key, timestamp).apply()
        Log.d("VenderGato", "Timestamp de última generación guardado para user $userId: $timestamp")
    }

    // Cargar el timestamp de la última generación de compradores (per user)
    private fun cargarUltimaGeneracionTimestamp(userId: Int): Long {
        val prefs = getSharedPreferences(KEY_LAST_GENERATION_TIMESTAMP_BASE, MODE_PRIVATE)
        val key = "${KEY_LAST_GENERATION_TIMESTAMP_BASE}_$userId" // User-specific key
        val timestamp = prefs.getLong(key, 0L) // Default to 0 if not found
        Log.d("VenderGato", "Timestamp de última generación cargado para user $userId: $timestamp")
        return timestamp
    }



    // Resetear Compradores a media noche. Ahora requiere el usuario y usa la DB y SP.
    private suspend fun updateRecyclerViewData(user: User) {
        val nuevosCompradores = selectAndSaveNewDailyBuyers(user) // Pasa el usuario
        withContext(Dispatchers.Main) {
            currentDailyBuyersList = nuevosCompradores // Actualizar la lista in-memory
            Toast.makeText(this@VenderGato, "¡Medianoche alcanzada! Recargando compradores...", Toast.LENGTH_SHORT).show()
            adapter.actualizarLista(currentDailyBuyersList) // Actualizar adapter
            adapter.selectedItemId = null // Deselect any previous buyer
        }
    }


    // Modified selling logic using the activity's currentDailyBuyersList and interacting with DB
    private fun resolverVenta() {
        val compradorSeleccionadoId = adapter.selectedItemId
        if (compradorSeleccionadoId != null && currentUser != null) {

            val compradorSeleccionadoConGato = currentDailyBuyersList.find { it.comprador.id == compradorSeleccionadoId }

            if (compradorSeleccionadoConGato != null) {
                lifecycleScope.launch(Dispatchers.IO) { // Cambiado a IO para operaciones de DB
                    val gatosUsuario = dbHelper.obtenerGatosByUser(currentUser!!)

                    val gatoEncontrado = gatosUsuario.find { it.nombre == compradorSeleccionadoConGato.nombreGatoInteres }

                    withContext(Dispatchers.Main) { // Volver al hilo principal para UI
                        if (gatoEncontrado != null) {
                            val dialogView = layoutInflater.inflate(R.layout.dialog_venta_completada, null)

                            val builder = android.app.AlertDialog.Builder(this@VenderGato)
                                .setView(dialogView)

                            val dialog = builder.create()

                            val mensaje = dialogView.findViewById<TextView>(R.id.mensajeVenta)
                            val btnAceptar = dialogView.findViewById<Button>(R.id.btnAceptarVenta)

                            mensaje.text = "Has vendido un gato a ${compradorSeleccionadoConGato.comprador.nombre}"

                            btnAceptar.setOnClickListener {

                                lifecycleScope.launch(Dispatchers.IO) {
                                    dbHelper.eliminarCompradorDeUsuario(compradorSeleccionadoId, currentUser!!.id!!)
                                    dbHelper.eliminarGatoDeUsuario(gatoEncontrado.id!!, currentUser!!.id!!)
                                    Log.d("VenderGato", "Eliminado comprador ${compradorSeleccionadoId} de la lista diaria de usuario ${currentUser!!.id!!}")

                                    // Ahora, actualizar la lista in-memory y el adapter en el hilo principal
                                    withContext(Dispatchers.Main) {
                                        currentDailyBuyersList = currentDailyBuyersList.filter { it.comprador.id != compradorSeleccionadoId }
                                        adapter.actualizarLista(currentDailyBuyersList) // Actualiza el adapter con la nueva lista filtrada
                                        adapter.selectedItemId = null
                                    }
                                }
                                dialog.dismiss()
                            }
                            dialog.show()
                        } else {
                            Toast.makeText(this@VenderGato, "No tienes un gato con el nombre que busca este comprador (${compradorSeleccionadoConGato.nombreGatoInteres})", Toast.LENGTH_SHORT).show()
                        }

                    }
                    startActivity(Intent(this@VenderGato, MainActivity::class.java))
                    finish()
                }

            } else {
                Toast.makeText(this, "Error: Comprador seleccionado no encontrado en la lista actual.", Toast.LENGTH_SHORT).show()
            }

        } else {
            val message = if (compradorSeleccionadoId == null) "Selecciona un comprador primero" else "Error: No se pudo cargar el usuario."
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoVenta(compradorConGato: CompradorConGato) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_info_comprador, null)

        val precioEditText = dialogView.findViewById<EditText>(R.id.precioEditText)
        val btnVender = dialogView.findViewById<Button>(R.id.btnVender)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnVender.setOnClickListener {
            val precioStr = precioEditText.text.toString()
            val precio = precioStr.toDoubleOrNull()

            if (precio == null || precio <= 0) {
                Toast.makeText(this, "Introduce un precio válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val comprador = compradorConGato.comprador

            if (comprador.dinero >= precio) {
                // Resta el precio al dinero del comprador
                comprador.dinero -= precio

                // Actualiza la BD en background
                lifecycleScope.launch(Dispatchers.IO) {
                    comprador.id?.let { it1 -> dbHelper.actualizarDineroComprador(it1, comprador.dinero) }
                }

                Toast.makeText(this, "Venta realizada por $precio €", Toast.LENGTH_SHORT).show()

                // Cierra el diálogo
                dialog.dismiss()

                // Actualiza UI (elimina comprador vendido)
                comprador.id?.let { it1 -> adapter.eliminarComprador(it1) }
                adapter.selectedItemId = null
                currentDailyBuyersList = currentDailyBuyersList.filter { it.comprador.id != comprador.id }

//                resolverVenta()

            } else {
                Toast.makeText(this, "El comprador no tiene suficiente dinero", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun intentarVenderDesdeClick(compradorConGato: CompradorConGato) {
        lifecycleScope.launch(Dispatchers.Main) {
            val prefs = applicationContext.getAppSharedPreferences()
            val user = prefs.getUserAsync("Usuario")!!
            val gatosUsuario = dbHelper.obtenerGatosByUser(user)

            val gatoEncontrado = gatosUsuario.find { it.nombre == compradorConGato.nombreGatoInteres }

            if (gatoEncontrado == null) {
                Toast.makeText(this@VenderGato, "No tienes un gato con ese nombre", Toast.LENGTH_SHORT).show()
                return@launch
            }

            mostrarDialogoVenta(compradorConGato)
        }
    }
}