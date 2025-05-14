package com.example.gatoshunter

import TemporizadorMedianoche
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VenderGato : AppCompatActivity() {

    // Adaptador para el RecyclerView
    private lateinit var adapter: CompradorAdapter

    // Temporizador
    private lateinit var timerTextView: TextView

    private val compradores = listOf(
        Comprador(1, "Paco", "Las palmas"),
        Comprador(2, "Pepe", "Telde"),
        Comprador(3, "Pacote", "Las palmas"),
        Comprador(4, "PePITO", "Telde")
    )

    //Los compradores aleaorios al abrir la app
    private var compradoresMostrados: List<Comprador>? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vender_gatos)

        // Inicialización de los botones
        val backButton: Button = findViewById(R.id.backbutton)
        val sellButton: Button = findViewById(R.id.sellbutton)

        //Timer
        timerTextView = findViewById(R.id.temporizador)

        // Inicialización del RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        if (compradoresMostrados == null) {
            compradoresMostrados = compradores.shuffled().take(3)
        }

        compradoresMostrados = cargarcompradoresMostradosDePrefs()
        if (compradoresMostrados == null) {
            compradoresMostrados = compradores.shuffled().take(3)
            guardarcompradoresMostradosEnPrefs(compradoresMostrados!!.map { it.id })
        }
        // Configuración del adaptador
        adapter = CompradorAdapter(compradoresMostrados!!)
        recyclerView.adapter = adapter


        // Configurar las acciones de los botones
        backButton.setOnClickListener {
            finish() // Vuelve a la actividad anterior
        }

        sellButton.setOnClickListener {
            if (adapter.selectedItemId != null) {
                val idSeleccionado = adapter.selectedItemId!!

                // Eliminar el gato de la lista visible
                adapter.eliminarComprador(idSeleccionado)
                adapter.selectedItemId = null
            } else {
                Toast.makeText(this, "Selecciona un comprador primero", Toast.LENGTH_SHORT).show()
            }
        }
        val temporizador = TemporizadorMedianoche(timerTextView) {
            // Acción que se ejecuta a medianoche
            // Por ejemplo: recargar lista, resetear contador, etc.
            updateRecyclerViewData()
        }
        // Inicia o restaura el temporizador
        temporizador.iniciar()
    }
    // Simula la actualización de datos del RecyclerView
    private fun updateRecyclerViewData() {
        // Obtén nuevos datos aquí (puede venir de una API o base de datos)
        compradoresMostrados = compradores.shuffled().take(3)
        guardarcompradoresMostradosEnPrefs(compradoresMostrados!!.map { it.id })
        adapter.actualizarLista(compradoresMostrados!!)
    }

    // Genera datos simulados
    private fun fetchNewData(): List<Comprador> {
        return compradores.shuffled().take(3)
    }

    private fun guardarcompradoresMostradosEnPrefs(ids: List<Int>) {
        val prefs = getSharedPreferences("GatosPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("gatos_ids", ids.joinToString(","))
        editor.apply()
    }

    private fun cargarcompradoresMostradosDePrefs(): List<Comprador>? {
        val prefs = getSharedPreferences("GatosPrefs", MODE_PRIVATE)
        val idsString = prefs.getString("gatos_ids", null) ?: return null
        val ids = idsString.split(",").mapNotNull { it.toIntOrNull() }

        return compradores.filter { it.id in ids }
    }
    
    

}
