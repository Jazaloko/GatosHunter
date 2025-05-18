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
import com.example.gatoshunter.adaptes.CompradorAdapter
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
        val compradorSeleccionadoId = adapter.selectedItemId
        if (compradorSeleccionadoId != null) {

            val compradorSeleccionado = currentDailyBuyersList.find { it.id == compradorSeleccionadoId }

            if (compradorSeleccionado != null) {
                val dialogView = layoutInflater.inflate(R.layout.dialog_venta_completada, null)

                val builder = android.app.AlertDialog.Builder(this)
                    .setView(dialogView)

                val dialog = builder.create()

                val mensaje = dialogView.findViewById<TextView>(R.id.mensajeVenta)
                val btnAceptar = dialogView.findViewById<Button>(R.id.btnAceptarVenta)

                mensaje.text = "Has vendido un gato a ${compradorSeleccionado.nombre}"

                btnAceptar.setOnClickListener {
                    adapter.eliminarComprador(compradorSeleccionadoId)
                    adapter.selectedItemId = null
                    currentDailyBuyersList = currentDailyBuyersList.filter { it.id != compradorSeleccionadoId }
                    dialog.dismiss()
                }

                dialog.show()
            } else {
                Toast.makeText(this, "Error: Comprador no encontrado.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Selecciona un comprador primero", Toast.LENGTH_SHORT).show()
        }
    }

}