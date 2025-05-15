package com.example.gatoshunter

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
import com.example.gatoshunter.clases.GatoAdapter
import com.example.miapp.database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.gatoshunter.TemporizadorMedianoche
import java.util.concurrent.TimeUnit

class BuscarGato : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: GatoAdapter
    private lateinit var timerTextView: TextView
    private val handler = Handler()
    private var ultimoGatoCompradoId: Int? = null
    private lateinit var temporizadorMedianoche: TemporizadorMedianoche

    // SharedPreferences constants
    private val PREFS_NAME = "GatosDiariosPrefs"
    private val KEY_GATO_IDS = "gato_ids_daily"

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

        // --- Cat Loading Logic (using filtering) ---
        val gatosToShow: List<Gato> = loadOrCreateDailyCats()
        adapter = GatoAdapter(gatosToShow) // Initialize adapter with the loaded/new cats
        recyclerView.adapter = adapter
        // --- End Cat Loading Logic ---

//        val gatos = dbHelper.obtenerGatosLibres()?.shuffled()?.take(3) ?: emptyList()
//        guardarGatosMostradosEnPrefs(gatos.mapNotNull { it.id })
//
//        adapter = GatoAdapter(gatos)
//        recyclerView.adapter = adapter

        backButton.setOnClickListener { finish() }

        buyButton.setOnClickListener { resolverCompra() }

        // --- Initialize and start the timer ---
        // TemporizadorMedianoche logic triggers the lambda at midnight.
        temporizadorMedianoche = TemporizadorMedianoche(timerTextView) {
            // This lambda executes when the timer reaches midnight
            lifecycleScope.launch(Dispatchers.IO) { // Perform DB operations on IO thread
                // Lógica para recargar los gatos:
                val allFreeCats = dbHelper.obtenerGatosLibres() // Get ALL free cats
                val nuevosGatos = allFreeCats.shuffled().take(3) // Select 3 random from current free cats
                val nuevosGatoIds = nuevosGatos.mapNotNull { it.id }

                // Save the new daily cat IDs (overwriting previous ones)
                guardarGatosDiarios(nuevosGatoIds)

                // Fetch the actual Gato objects for the adapter by filtering
                // The list `nuevosGatos` already contains the selected cats,
                // but re-filtering from `allFreeCats` ensures consistency if the
                // list was modified between fetching all and shuffling.
                // However, since we just selected them, `nuevosGatos` is correct.
                // Let's directly use `nuevosGatos` to avoid unnecessary re-filtering right after selection.
                val gatosParaAdapter = nuevosGatos


                runOnUiThread {
                    Toast.makeText(this@BuscarGato, "¡Medianoche alcanzada! Recargando gatos...", Toast.LENGTH_SHORT).show()
                    adapter.actualizarLista(gatosParaAdapter) // Update adapter with new list
                    adapter.selectedItemId = null // Deselect any previous cat
                }
            }
            // The timer restarts automatically for the next day within the TemporizadorMedianoche class
        }
        temporizadorMedianoche.iniciar()


    }

    // Function to load saved daily cat IDs or select new ones
    private fun loadOrCreateDailyCats(): List<Gato> {
        val savedCatIds = cargarGatosDiarios() // Load IDs from SharedPreferences
        val allFreeCats = dbHelper.obtenerGatosLibres() // Get ALL free cats from DB

        return if (savedCatIds.isNotEmpty()) {
            // Filter the list in memory based on saved IDs
            allFreeCats.filter { it.id in savedCatIds }
        } else {
            // No daily cats saved, select new ones and save their IDs
            val selectedCats = allFreeCats.shuffled().take(3) // Select 3 random
            val selectedCatIds = selectedCats.mapNotNull { it.id }
            guardarGatosDiarios(selectedCatIds) // Save the selected IDs for the day
            selectedCats // Return the newly selected cats
        }
    }


    // Helper function to save daily cat IDs to SharedPreferences
    private fun guardarGatosDiarios(ids: List<Int>) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        // Store IDs as a comma-separated string
        prefs.edit().putString(KEY_GATO_IDS, ids.joinToString(",")).apply()
    }



    private fun cargarGatosDiarios(): List<Int> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val idsString = prefs.getString(KEY_GATO_IDS, null)
        // If string is null or empty, return empty list
        return if (idsString.isNullOrEmpty()) {
            emptyList()
        } else {
            // Split the string by comma and convert each part to an integer
            try {
                idsString.split(",").mapNotNull { it.toIntOrNull() }
            } catch (e: Exception) {
                // Handle potential parsing errors, return empty list
                Log.e("BuscarGato", "Error parsing gato_ids_daily from SharedPreferences", e)
                emptyList()
            }
        }
    }


    private fun resolverCompra() {
        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = applicationContext.getAppSharedPreferences()
            val user = prefs.getUserAsync("Usuario")

            val gatoSeleccionado = adapter.getGatoSeleccionado()
            if (gatoSeleccionado != null && user != null) {
                dbHelper.insertarGatoUser(gatoSeleccionado, user)
                dbHelper.eliminarGatoLibre(gatoSeleccionado.id!!)
                ultimoGatoCompradoId = gatoSeleccionado.id

                runOnUiThread {
                    adapter.eliminarGato(gatoSeleccionado.id!!)
                    adapter.selectedItemId = null
                    mostrarDialogoExito()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this@BuscarGato, "Selecciona un gato primero", Toast.LENGTH_SHORT).show()
                }
            }
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
            mostrarDialogoNombreGato()
        }

        dialog.show()
    }

    private fun mostrarDialogoNombreGato() {
        val input = EditText(this).apply {
            hint = "Ej: Pelusa"
            setPadding(50, 40, 50, 40)
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("Ponle un nombre a tu gato")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Guardar") { d, _ ->
                val nuevoNombre = input.text.toString().trim()
                if (nuevoNombre.isNotEmpty() && ultimoGatoCompradoId != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        dbHelper.actualizarNombreGato(ultimoGatoCompradoId!!, nuevoNombre)
                    }
                    Toast.makeText(this, "¡Nombre guardado!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Nombre no válido", Toast.LENGTH_SHORT).show()
                }
                d.dismiss()
            }
            .show()
    }

    private fun guardarGatosMostradosEnPrefs(ids: List<Int>) {
        val prefs = getSharedPreferences("GatosPrefs", MODE_PRIVATE)
        prefs.edit().putString("gatos_ids", ids.joinToString(",")).apply()
    }


}
