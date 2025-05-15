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

class VenderGato : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper // dbHelper is used now

    private lateinit var adapter: CompradorAdapter
    private lateinit var timerTextView: TextView
    private lateinit var temporizadorMedianoche: TemporizadorMedianoche

    private var currentDailyBuyersList: List<Comprador> = emptyList()

    private val COMPRADORES_PREFS_NAME = "CompradoresDiariosPrefs"
    private val KEY_COMPRADOR_IDS = "comprador_ids_daily"

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

        //Botone
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

    //Cargar compradores diarios
    private fun loadOrCreateDailyBuyers(): List<Comprador> {
        val savedBuyerIds = cargarCompradoresDiarios()
        val allPotentialBuyers = dbHelper.obtenerCompradores()

        return if (savedBuyerIds.isNotEmpty()) {
            val dailyBuyers = allPotentialBuyers.filter { it.id in savedBuyerIds }
            if (dailyBuyers.size < 3) {
                Log.w("VenderGato", "Less than 3 daily buyers found from saved IDs. Refreshing...")
                selectAndSaveNewDailyBuyers(allPotentialBuyers) // Select new ones if saved list is incomplete
            } else {
                dailyBuyers
            }
        } else {
            selectAndSaveNewDailyBuyers(allPotentialBuyers)
        }
    }

    // Helper function to select 3 random buyers and save their IDs
    private fun selectAndSaveNewDailyBuyers(allPotentialBuyers: List<Comprador>): List<Comprador> {
        val selectedBuyers = allPotentialBuyers.shuffled().take(3)
        val selectedBuyerIds = selectedBuyers.mapNotNull { it.id }
        guardarCompradoresDiarios(selectedBuyerIds)
        return selectedBuyers
    }


    // Guardar los compradores del dia
    private fun guardarCompradoresDiarios(ids: List<Int>) {
        val prefs = getSharedPreferences(COMPRADORES_PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_COMPRADOR_IDS, ids.joinToString(",")).apply()
    }

    // Cargar los compradores del dia
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

    // Resetear Compradores a media noche
    private suspend fun updateRecyclerViewData() {
        val allPotentialBuyers = dbHelper.obtenerCompradores() // Get ALL potential buyers from DB
        val nuevosCompradores = selectAndSaveNewDailyBuyers(allPotentialBuyers) // Select new ones and save IDs

        withContext(Dispatchers.Main) {
            currentDailyBuyersList = nuevosCompradores // Actualizar la lista
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
                // Now we have the Comprador object with its details (like name)
                Log.d("VenderGato", "Selected buyer: ${compradorSeleccionado.nombre}")

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