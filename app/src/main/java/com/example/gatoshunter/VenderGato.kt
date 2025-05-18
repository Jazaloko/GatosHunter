package com.example.gatoshunter

import android.os.Bundle
import android.os.Parcelable
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
import com.example.gatoshunter.clases.Gato
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

    private var currentDailyBuyersList: List<CompradorConGato> = emptyList()

    private val COMPRADORES_PREFS_NAME = "CompradoresDiariosPrefs"
    private val GATOS_PREFS_NAME = "GatosDiariosPrefs"
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
        adapter = CompradorAdapter(emptyList())
        recyclerView.adapter = adapter


        lifecycleScope.launch(Dispatchers.IO) {
            currentDailyBuyersList = loadOrCreateDailyBuyers()
            withContext(Dispatchers.Main) {
                adapter.actualizarLista(currentDailyBuyersList)
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
    private fun loadOrCreateDailyBuyers(): List<CompradorConGato> {
        val savedBuyersWithCats = cargarCompradoresDiarios()
        val lastTimestamp = cargarUltimaGeneracionTimestamp()
        val currentCalendar = Calendar.getInstance()
        val lastGenerationCalendar = Calendar.getInstance().apply { timeInMillis = lastTimestamp }

        val isSameDay = lastTimestamp != 0L &&
                currentCalendar.get(Calendar.YEAR) == lastGenerationCalendar.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.DAY_OF_YEAR) == lastGenerationCalendar.get(Calendar.DAY_OF_YEAR)

        val allPotentialBuyers = dbHelper.obtenerCompradores()
        val allPotentialGatos = dbHelper.obtenerGatos()

        return if (isSameDay) {
            // Si es el mismo día, intenta cargar los compradores con sus gatos guardados.
            savedBuyersWithCats.mapNotNull { (buyerId, catName) ->
                allPotentialBuyers.find { it.id == buyerId }?.let { CompradorConGato(it, catName) }
            }

        } else {
            // Si es un nuevo día, genera nuevos compradores y asigna gatos aleatorios.
            Log.d("VenderGato", "Nuevo día o no timestamp. Generando nuevos compradores con gatos.")
            selectAndSaveNewDailyBuyers(allPotentialBuyers, allPotentialGatos)
        }

    }

    data class CompradorConGato(val comprador: Comprador, val nombreGatoInteres: String?)

    // Helper function to select 3 random buyers and save their IDs and the current timestamp
    private fun selectAndSaveNewDailyBuyers(allPotentialBuyers: List<Comprador>, gatos: List<Gato>): List<CompradorConGato> {
        // Ensure we don't select more buyers than available
        val numberOfBuyersToSelect = minOf(3, allPotentialBuyers.size)
        val selectedBuyers = allPotentialBuyers.shuffled().take(numberOfBuyersToSelect)
        val selectedGatos = gatos.shuffled().take(numberOfBuyersToSelect)

        val compradoresConGato = selectedBuyers.zip(selectedGatos) { comprador, gato ->
            CompradorConGato(comprador, gato.nombre)
        }

        // Save the new list of IDs and the current timestamp
        guardarCompradoresDiarios(
            compradoresConGato.mapNotNull { it.comprador.id },
            compradoresConGato.mapNotNull { it.nombreGatoInteres }
        )
        guardarUltimaGeneracionTimestamp(System.currentTimeMillis())

        Log.d("VenderGato", "Selected and saved new daily buyers with cats: ${compradoresConGato.joinToString { "${it.comprador.nombre} -> ${it.nombreGatoInteres}" }}")
        return compradoresConGato
    }


    // Guardar los compradores del dia (IDs)
    private fun guardarCompradoresDiarios(ids: List<Int>, gatosNames: List<String>) {
        val prefs = getSharedPreferences(COMPRADORES_PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_COMPRADOR_IDS, ids.joinToString(",")).apply()
        prefs.edit().putString(GATOS_PREFS_NAME, gatosNames.joinToString(",")).apply()
    }

    // Cargar los compradores del dia (IDs) con los nombres de los gatos
    private fun cargarCompradoresDiarios(): List<Pair<Int, String>> {
        val prefs = getSharedPreferences(COMPRADORES_PREFS_NAME, MODE_PRIVATE)
        val idsString = prefs.getString(KEY_COMPRADOR_IDS, null)
        val gatosNames = prefs.getString(GATOS_PREFS_NAME, null)
        return if (idsString.isNullOrEmpty() || gatosNames.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                val ids = idsString.split(",").mapNotNull { it.toIntOrNull() }
                val nombresDeGatos = gatosNames.split(",")
                // Asegurarse de que ambas listas tengan la misma cantidad de elementos
                if (ids.size == nombresDeGatos.size) {
                    ids.zip(nombresDeGatos) { id, nombreGato -> Pair(id, nombreGato) }
                } else {
                    Log.w("VenderGato", "Mismatch en la cantidad de IDs de compradores y nombres de gatos guardados.")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("VenderGato", "Error al parsear los compradores o los nombres de los gatos desde SharedPreferences", e)
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
        val allPotentialGatos = dbHelper.obtenerGatos() // Get ALL potential gatos from DB
        val nuevosCompradores = selectAndSaveNewDailyBuyers(allPotentialBuyers, allPotentialGatos) // Select new ones and save IDs and timestamp

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

            val compradorSeleccionado = currentDailyBuyersList.find { it.comprador.id == compradorSeleccionadoId }

            if (compradorSeleccionado != null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    val prefs = applicationContext.getAppSharedPreferences()
                    val user = prefs.getUserAsync("Usuario")!!
                    val gatosUsuario = dbHelper.obtenerGatosByUser(user)

                    gatosUsuario.forEach {
                        if (it.nombre == compradorSeleccionado.nombreGatoInteres){
                            val dialogView = layoutInflater.inflate(R.layout.dialog_venta_completada, null)

                            val builder = android.app.AlertDialog.Builder(this@VenderGato)
                                .setView(dialogView)

                            val dialog = builder.create()

                            val mensaje = dialogView.findViewById<TextView>(R.id.mensajeVenta)
                            val btnAceptar = dialogView.findViewById<Button>(R.id.btnAceptarVenta)

                            mensaje.text = "Has vendido un gato a ${compradorSeleccionado.comprador.nombre}"

                            btnAceptar.setOnClickListener {
                                adapter.eliminarComprador(compradorSeleccionadoId)
                                adapter.selectedItemId = null
                                currentDailyBuyersList = currentDailyBuyersList.filter { it.comprador.id != compradorSeleccionadoId }
                                dialog.dismiss()
                            }
                            dialog.show()
                        }
                    }

                }

            } else {
                Toast.makeText(this, "Error: Comprador no encontrado.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Selecciona un comprador primero", Toast.LENGTH_SHORT).show()
        }
    }

}