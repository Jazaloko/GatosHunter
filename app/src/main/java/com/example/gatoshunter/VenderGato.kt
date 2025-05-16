package com.example.gatoshunter

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.clases.Comprador
import com.example.gatoshunter.clases.CompradorAdapter
import com.example.miapp.database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class VenderGato : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var adapter: CompradorAdapter
    private lateinit var timerTextView: TextView
    private lateinit var temporizadorMedianoche: TemporizadorMedianoche

    private var currentDailyBuyersList: List<Comprador> = emptyList()

    private val COMPRADORES_PREFS_NAME = "CompradoresDiariosPrefs"
    private val KEY_COMPRADOR_IDS = "comprador_ids_daily"
    private val KEY_LAST_GENERATION_TIMESTAMP = "last_generation_timestamp"

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


        lifecycleScope.launch(Dispatchers.IO) {
            currentDailyBuyersList = loadOrCreateDailyBuyers()
            withContext(Dispatchers.Main) {
                // Initialize adapter on the main thread
                adapter = CompradorAdapter(currentDailyBuyersList)
                recyclerView.adapter = adapter
            }
        }

        //Botones
        backButton.setOnClickListener { finish() }

        sellButton.setOnClickListener { resolverVenta() }

        // Timer
        temporizadorMedianoche = TemporizadorMedianoche(timerTextView) {
            lifecycleScope.launch(Dispatchers.IO) {
                updateRecyclerViewData()
            }
        }
        temporizadorMedianoche.iniciar()
    }

    //Cargar compradores diarios o crear si es un nuevo día
    private fun loadOrCreateDailyBuyers(): List<Comprador> {
        val savedBuyerIds = cargarCompradoresDiarios()
        val lastTimestamp = cargarUltimaGeneracionTimestamp()
        val currentCalendar = Calendar.getInstance()
        val lastGenerationCalendar = Calendar.getInstance().apply { timeInMillis = lastTimestamp }

        val isSameDay = lastTimestamp != 0L &&
                currentCalendar.get(Calendar.YEAR) == lastGenerationCalendar.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.DAY_OF_YEAR) == lastGenerationCalendar.get(Calendar.DAY_OF_YEAR)

        val allPotentialBuyers = dbHelper.obtenerCompradores()

        return if (isSameDay) {
            // If it's the same day, load the saved buyers.
            // If savedBuyerIds is empty, it means all were sold today, so we return an empty list.
            val dailyBuyers = allPotentialBuyers.filter { it.id in savedBuyerIds }
            if (dailyBuyers.size != savedBuyerIds.size) {
                Log.w("VenderGato", "Mismatch between saved IDs count (${savedBuyerIds.size}) and found buyers count (${dailyBuyers.size}). This might indicate a buyer was deleted from the database.")
            }
            Log.d("VenderGato", "Loading buyers for the same day. Saved IDs: ${savedBuyerIds.joinToString(",")}")
            dailyBuyers
        } else {
            // If it's a new day or no timestamp saved, generate new buyers.
            Log.d("VenderGato", "New day or no timestamp. Generating new buyers.")
            selectAndSaveNewDailyBuyers(allPotentialBuyers)
        }
    }

    // Helper function to select 3 random buyers and save their IDs and the current timestamp
    private fun selectAndSaveNewDailyBuyers(allPotentialBuyers: List<Comprador>): List<Comprador> {
        // Ensure we don't select more buyers than available
        val numberOfBuyersToSelect = minOf(3, allPotentialBuyers.size)
        val selectedBuyers = allPotentialBuyers.shuffled().take(numberOfBuyersToSelect)
        val selectedBuyerIds = selectedBuyers.mapNotNull { it.id }

        // Save the new list of IDs and the current timestamp
        guardarCompradoresDiarios(selectedBuyerIds)
        guardarUltimaGeneracionTimestamp(System.currentTimeMillis())

        Log.d("VenderGato", "Selected and saved new daily buyer IDs: ${selectedBuyerIds.joinToString(",")}")
        return selectedBuyers
    }


    // Guardar los compradores del dia (IDs)
    private fun guardarCompradoresDiarios(ids: List<Int>) {
        val prefs = getSharedPreferences(COMPRADORES_PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_COMPRADOR_IDS, ids.joinToString(",")).apply()
    }

    // Cargar los compradores del dia (IDs)
    private fun cargarCompradoresDiarios(): List<Int> {
        val prefs = getSharedPreferences(COMPRADORES_PREFS_NAME, MODE_PRIVATE)
        val idsString = prefs.getString(KEY_COMPRADOR_IDS, null)
        return if (idsString.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                idsString.split(",").mapNotNull { it.toIntOrNull() }
            } catch (e: Exception) {
                Log.e("VenderGato", "Error parsing comprador_ids_daily from SharedPreferences", e)
                emptyList()
            }
        }
    }

    // Guardar el timestamp de la última generación de compradores
    private fun guardarUltimaGeneracionTimestamp(timestamp: Long) {
        val prefs = getSharedPreferences(COMPRADORES_PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_GENERATION_TIMESTAMP, timestamp).apply()
    }

    // Cargar el timestamp de la última generación de compradores
    private fun cargarUltimaGeneracionTimestamp(): Long {
        val prefs = getSharedPreferences(COMPRADORES_PREFS_NAME, MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_GENERATION_TIMESTAMP, 0L) // Default to 0 if not found
    }

    // Resetear Compradores a media noche
    private suspend fun updateRecyclerViewData() {
        val allPotentialBuyers = dbHelper.obtenerCompradores() // Get ALL potential buyers from DB
        val nuevosCompradores = selectAndSaveNewDailyBuyers(allPotentialBuyers) // Select new ones and save IDs and timestamp

        withContext(Dispatchers.Main) {
            currentDailyBuyersList = nuevosCompradores // Actualizar la lista in-memory
            Toast.makeText(this@VenderGato, "¡Medianoche alcanzada! Recargando compradores...", Toast.LENGTH_SHORT).show()
            adapter.actualizarLista(currentDailyBuyersList) // Actualizar adapter
            adapter.selectedItemId = null // Deselect any previous buyer
        }
    }


    // Modified selling logic using the activity's currentDailyBuyersList
    private fun resolverVenta() {
        val compradorSeleccionadoId = adapter.selectedItemId // Get ID from adapter
        if (compradorSeleccionadoId != null) {

            val compradorSeleccionado = currentDailyBuyersList.find { it.id == compradorSeleccionadoId }

            if (compradorSeleccionado != null) {
                Log.d("VenderGato", "Selected buyer: ${compradorSeleccionado.nombre}")

                // --- START: Logic to remove buyer from SharedPreferences ---
                val currentSavedIds = cargarCompradoresDiarios().toMutableList() // Load current saved IDs
                if (currentSavedIds.remove(compradorSeleccionadoId)) { // Attempt to remove the ID from the list
                    guardarCompradoresDiarios(currentSavedIds) // Save the updated list back to SharedPreferences
                    Log.d("VenderGato", "Removed buyer ID $compradorSeleccionadoId from SharedPreferences.")
                } else {
                    Log.w("VenderGato", "Buyer ID $compradorSeleccionadoId not found in SharedPreferences list.")
                }
                // --- END: Logic to remove buyer from SharedPreferences ---


                // Remove buyer from adapter's visible list (UI update)
                adapter.eliminarComprador(compradorSeleccionadoId)
                adapter.selectedItemId = null // Deselect

                // Also update the activity's list to keep it in sync for future sales in this session
                // This prevents finding a buyer that has just been "sold" within the same day's list.
                currentDailyBuyersList = currentDailyBuyersList.filter { it.id != compradorSeleccionadoId }

                // Use the fetched buyer's name in the Toast
                Toast.makeText(this, "Comprador ${compradorSeleccionado.nombre} se ha ido.", Toast.LENGTH_SHORT).show()

            } else {
                Log.e("VenderGato", "Comprador with ID $compradorSeleccionadoId not found in activity's current daily list.")
                Toast.makeText(this, "Error: Comprador no encontrado en la lista actual.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Selecciona un comprador primero", Toast.LENGTH_SHORT).show()
        }
    }
}