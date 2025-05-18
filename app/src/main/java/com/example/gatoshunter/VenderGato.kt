package com.example.gatoshunter

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.adaptes.CompradorAdapter
import com.example.gatoshunter.adaptes.OnCompradorDoubleClickListener
import com.example.gatoshunter.clases.Comprador
import com.example.gatoshunter.clases.User
import com.example.miapp.database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class VenderGato : AppCompatActivity(), OnCompradorDoubleClickListener {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: CompradorAdapter
    private lateinit var timerTextView: TextView
    private lateinit var temporizadorMedianoche: TemporizadorMedianoche
    private lateinit var currentUser: User

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
        currentUser = obtenerUsuarioDesdePreferencias()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        lifecycleScope.launch(Dispatchers.IO) {
            currentDailyBuyersList = loadOrCreateDailyBuyers()
            withContext(Dispatchers.Main) {
                adapter = CompradorAdapter(currentDailyBuyersList, this@VenderGato)
                recyclerView.adapter = adapter
            }
        }

        backButton.setOnClickListener { finish() }
        sellButton.setOnClickListener { resolverVenta() }

        temporizadorMedianoche = TemporizadorMedianoche(timerTextView) {
            lifecycleScope.launch(Dispatchers.IO) {
                updateRecyclerViewData()
            }
        }
        temporizadorMedianoche.iniciar()
    }

    override fun onCompradorDoubleClick(comprador: Comprador) {
        mostrarDialogoVenta(comprador, currentUser)
    }

    private fun obtenerUsuarioDesdePreferencias(): User {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val id = prefs.getInt("user_id", -1)
        val nombre = prefs.getString("user_nombre", null)
        return User(id = id, nombre = nombre ?: "Desconocido", null.toString(),0.0,null)
    }

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
            allPotentialBuyers.filter { it.id in savedBuyerIds }
        } else {
            selectAndSaveNewDailyBuyers(allPotentialBuyers)
        }
    }

    private fun selectAndSaveNewDailyBuyers(allPotentialBuyers: List<Comprador>): List<Comprador> {
        val numberOfBuyersToSelect = minOf(3, allPotentialBuyers.size)
        val selectedBuyers = allPotentialBuyers.shuffled().take(numberOfBuyersToSelect)
        val selectedBuyerIds = selectedBuyers.mapNotNull { it.id }

        guardarCompradoresDiarios(selectedBuyerIds)
        guardarUltimaGeneracionTimestamp(System.currentTimeMillis())

        return selectedBuyers
    }

    private fun guardarCompradoresDiarios(ids: List<Int>) {
        val prefs = getSharedPreferences(COMPRADORES_PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_COMPRADOR_IDS, ids.joinToString(",")).apply()
    }

    private fun cargarCompradoresDiarios(): List<Int> {
        val prefs = getSharedPreferences(COMPRADORES_PREFS_NAME, MODE_PRIVATE)
        val idsString = prefs.getString(KEY_COMPRADOR_IDS, null)
        return if (idsString.isNullOrEmpty()) {
            emptyList()
        } else {
            idsString.split(",").mapNotNull { it.toIntOrNull() }
        }
    }

    private fun guardarUltimaGeneracionTimestamp(timestamp: Long) {
        val prefs = getSharedPreferences(COMPRADORES_PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_GENERATION_TIMESTAMP, timestamp).apply()
    }

    private fun cargarUltimaGeneracionTimestamp(): Long {
        val prefs = getSharedPreferences(COMPRADORES_PREFS_NAME, MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_GENERATION_TIMESTAMP, 0L)
    }

    private suspend fun updateRecyclerViewData() {
        val allPotentialBuyers = dbHelper.obtenerCompradores()
        val nuevosCompradores = selectAndSaveNewDailyBuyers(allPotentialBuyers)

        withContext(Dispatchers.Main) {
            currentDailyBuyersList = nuevosCompradores
            adapter.actualizarLista(currentDailyBuyersList)
            adapter.selectedItemId = null
            Toast.makeText(this@VenderGato, "¡Medianoche alcanzada! Recargando compradores...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resolverVenta() {
        val compradorSeleccionadoId = adapter.selectedItemId
        if (compradorSeleccionadoId != null) {
            val compradorSeleccionado = currentDailyBuyersList.find { it.id == compradorSeleccionadoId }
            if (compradorSeleccionado != null) {
                mostrarDialogoVenta(compradorSeleccionado, currentUser)
            } else {
                Toast.makeText(this, "Error: Comprador no encontrado.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Selecciona un comprador primero", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoVenta(comprador: Comprador, user: User) {
        val gatosDisponibles = user.id?.let { dbHelper.obtenerGatosPorUserId(it) }

        if (gatosDisponibles.isNullOrEmpty()) {
            Toast.makeText(this, "No hay gatos disponibles para vender para este usuario.", Toast.LENGTH_SHORT).show()
            return
        }

        val gatoAleatorio = gatosDisponibles.random()
        val dialogView = layoutInflater.inflate(R.layout.activity_venta, null)

        val mensaje = dialogView.findViewById<TextView>(R.id.mensajeVenta)
        val inputPrecio = dialogView.findViewById<EditText>(R.id.inputPrecio)
        val btnAceptar = dialogView.findViewById<Button>(R.id.btnAceptarVenta)

        val builder = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val dialog = builder.create()

        mensaje.text = """
            Comprador: ${comprador.nombre}
            Dinero: ${comprador.dinero} €
            Gato: ${gatoAleatorio.nombre}
            Emoción: ${gatoAleatorio.emocion}
        """.trimIndent()

        btnAceptar.setOnClickListener {
            val precio = inputPrecio.text.toString().toDoubleOrNull()
            if (precio == null || precio <= 0) {
                Toast.makeText(this, "Introduce un precio válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (comprador.dinero >= precio) {
                comprador.dinero -= precio
                dbHelper.actualizarComprador(comprador)
                dbHelper.eliminarGatoDeUsuario(gatoAleatorio.id, user.id)

                comprador.id?.let { adapter.eliminarComprador(it) }
                adapter.selectedItemId = null
                currentDailyBuyersList = currentDailyBuyersList.filter { it.id != comprador.id }

                Toast.makeText(this, "Venta completada. Gato vendido por $precio €", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "El comprador no tiene suficiente dinero.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
