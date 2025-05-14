package com.example.gatoshunter

import TemporizadorMedianoche
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miapp.database.DatabaseHelper

class BuscarGato : AppCompatActivity() {

    // Base de datos
    private lateinit var dbHelper: DatabaseHelper

    // Adaptador para el RecyclerView
    private lateinit var adapter: GatoAdapter

    // Temporizador
    private lateinit var timerTextView: TextView

    // Handler y Runnable para actualización en tiempo real
    private val handler = Handler()

    //Los gatos aleaorios al abrir la app
    private var gatosMostrados: List<Gato>? = null


    private val gatos = listOf(
        Gato(1, "Gato1", 4.5, "Ciudad A", "Gato muy juguetón"),
        Gato(2, "Gato2", 3.2, "Ciudad B", "Gato tranquilo"),
        Gato(3, "Gato3", 5.0, "Ciudad C", "Gato curioso"),
        Gato(4, "Gato4", 3.2, "Ciudad D", "Gato tranquilo"),
        Gato(5, "Gato5", 5.0, "Ciudad E", "Gato curioso")
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buscar_gatos)

        // Inicialización de los botones
        val backButton: Button = findViewById(R.id.backbutton)
        val buyButton: Button = findViewById(R.id.buybutton)

        //Timer
        timerTextView = findViewById(R.id.temporizador)

        // Inicialización del RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        if (gatosMostrados == null) {
            gatosMostrados = gatos.shuffled().take(3)
        }

        gatosMostrados = cargarGatosMostradosDePrefs()
        if (gatosMostrados == null) {
            gatosMostrados = gatos.shuffled().take(3)
            guardarGatosMostradosEnPrefs(gatosMostrados!!.map { it.id })
        }


        // Configuración del adaptador
        adapter = GatoAdapter(gatosMostrados!!)
        recyclerView.adapter = adapter


        // Configurar las acciones de los botones
        backButton.setOnClickListener {
            finish() // Vuelve a la actividad anterior
        }

        buyButton.setOnClickListener {
            if (adapter.selectedItemId != null) {
                val idSeleccionado = adapter.selectedItemId!!

                // Aquí puedes eliminar el gato de la base de datos
                // dbHelper.eliminarGato(idSeleccionado)

                // Eliminar el gato de la lista visible
                adapter.eliminarGato(idSeleccionado)
                adapter.selectedItemId = null
            } else {
                Toast.makeText(this, "Selecciona un gato Primero", Toast.LENGTH_SHORT).show()
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
        gatosMostrados = gatos.shuffled().take(3)
        guardarGatosMostradosEnPrefs(gatosMostrados!!.map { it.id })
        adapter.actualizarLista(gatosMostrados!!)
    }

    // Genera datos simulados
    private fun fetchNewData(): List<Gato> {
        return gatos.shuffled().take(3)
    }

    private fun guardarGatosMostradosEnPrefs(ids: List<Int>) {
        val prefs = getSharedPreferences("GatosPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("gatos_ids", ids.joinToString(","))
        editor.apply()
    }

    private fun cargarGatosMostradosDePrefs(): List<Gato>? {
        val prefs = getSharedPreferences("GatosPrefs", MODE_PRIVATE)
        val idsString = prefs.getString("gatos_ids", null) ?: return null
        val ids = idsString.split(",").mapNotNull { it.toIntOrNull() }

        return gatos.filter { it.id in ids }
    }


}
