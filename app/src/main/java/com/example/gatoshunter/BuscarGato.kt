package com.example.gatoshunter

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miapp.database.DatabaseHelper
import java.util.concurrent.TimeUnit

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
        timerTextView = findViewById(R.id.Temporizador)

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

        // Inicia o restaura el temporizador
        startMidnightCountdown()
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
    private fun getMillisUntilMidnight(): Long {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1) // Nos movemos al próximo día a las 00:00
        }
        return midnight.timeInMillis - now.timeInMillis
    }

    private fun startMidnightCountdown() {
        handler.post(object : Runnable {
            override fun run() {
                val millisRemaining = getMillisUntilMidnight()

                if (millisRemaining > 0) {
                    updateTimerText(millisRemaining)
                    handler.postDelayed(this, 1000) // Repite cada segundo
                } else {
                    // Medianoche alcanzada
                    timerTextView.text = "Tiempo restante: 00:00:00"
                    updateRecyclerViewData() // Aquí puedes actualizar tu lista de gatos
                    handler.postDelayed(this, 1000) // Reiniciar para el nuevo día
                }
            }
        })
    }

    private fun updateTimerText(millis: Long) {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        timerTextView.text = String.format("Tiempo restante: %02d:%02d:%02d", hours, minutes, seconds)
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
