package com.example.gatoshunter

import android.annotation.SuppressLint
import android.content.SharedPreferences
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
    private val timerDuration = 10 * 60 * 1000L // 10 minutos en milisegundos
    private lateinit var sharedPreferences: SharedPreferences

    // Handler y Runnable para actualización en tiempo real
    private val handler = Handler()
    private var startTime = 0L // Guardar el tiempo de inicio del temporizador
    private var elapsedTime = 0L // Tiempo transcurrido desde que el temporizador empezó
    private var remainingTime = timerDuration // Tiempo restante del temporizador

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buscar_gatos)

        // Inicialización de los botones
        val backButton: Button = findViewById(R.id.backbutton)
        val buyButton: Button = findViewById(R.id.buybutton)

        // Inicialización del RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        // Lista de gatos inicial
        val gatos = listOf(
            Gato(1, "Gato1", 4.5, "Ciudad A", "Gato muy juguetón"),
            Gato(2, "Gato2", 3.2, "Ciudad B", "Gato tranquilo"),
            Gato(3, "Gato3", 5.0, "Ciudad C", "Gato curioso")
        )

        // Configuración del adaptador
        adapter = GatoAdapter(gatos)
        recyclerView.adapter = adapter

        // Inicialización de SharedPreferences para el temporizador
        sharedPreferences = getSharedPreferences("TimerPrefs", MODE_PRIVATE)
        startTime = sharedPreferences.getLong("startTime", 0L) // Cargar el tiempo de inicio
        elapsedTime = sharedPreferences.getLong("elapsedTime", 0L) // Tiempo transcurrido
        remainingTime = timerDuration - elapsedTime // Calcular el tiempo restante

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
        updateTimerUI()

    }

    // Método para actualizar la interfaz del temporizador
    private fun updateTimerUI() {
        if (remainingTime > 0) {
            updateTimerText(remainingTime)
            handler.postDelayed(timerRunnable, 1000) // Actualiza el temporizador cada segundo
        } else {
            onTimerFinished()
        }
    }

    // Lógica para manejar el final del temporizador
    private fun onTimerFinished() {
        findViewById<TextView>(R.id.Temporizador).text = "00:00"
        sharedPreferences.edit().remove("startTime").apply() // Borra el tiempo de inicio guardado
        sharedPreferences.edit().remove("elapsedTime").apply() // Borra el tiempo transcurrido guardado
        updateRecyclerViewData() // Actualiza los datos del RecyclerView con nuevos datos
        startNewTimer() // Reinicia el temporizador
    }

    // Reinicia el temporizador guardando el nuevo tiempo de inicio
    private fun startNewTimer() {
        startTime = System.currentTimeMillis() // Establece el nuevo tiempo de inicio
        sharedPreferences.edit().putLong("startTime", startTime).apply() // Guarda el nuevo tiempo de inicio
        remainingTime = timerDuration // Resetea el tiempo restante
        updateTimerUI() // Actualiza la interfaz para reflejar el nuevo tiempo
    }

    // Actualiza el texto del temporizador en la interfaz
    private fun updateTimerText(remainingTime: Long) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime) % 60
        findViewById<TextView>(R.id.Temporizador).text =
            "Tiempo restante: ${String.format("%02d:%02d", minutes, seconds)}"
    }

    // Runnable para actualizar el temporizador cada segundo
    private val timerRunnable = object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis()
            elapsedTime = currentTime - startTime // Calcula el tiempo transcurrido
            remainingTime = timerDuration - elapsedTime // Calcula el tiempo restante

            if (remainingTime > 0) {
                updateTimerText(remainingTime)
                handler.postDelayed(this, 1000) // Actualiza el temporizador en 1 segundo
            } else {
                onTimerFinished() // Si el temporizador llega a 0, finaliza
            }
        }
    }

    // Guarda el tiempo de inicio cuando la app se pausa
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timerRunnable) // Detiene el Runnable
        sharedPreferences.edit().putLong("startTime", startTime).apply() // Guarda el tiempo de inicio cuando la app se pausa
        sharedPreferences.edit().putLong("elapsedTime", elapsedTime).apply() // Guarda el tiempo transcurrido
    }

    // Calcula el tiempo restante al reanudar la actividad
    override fun onResume() {
        super.onResume()

        // Actualizamos inmediatamente el temporizador en la UI para evitar el parpadeo
        updateTimerUI()

        // Inicia el Runnable para actualizaciones constantes
        handler.post(timerRunnable)
    }

    // Simula la actualización de datos del RecyclerView
    private fun updateRecyclerViewData() {
        // Obtén nuevos datos aquí (puede venir de una API o base de datos)
        val newData = fetchNewData()
        adapter.actualizarLista(newData) // Actualiza el adaptador con los nuevos datos
    }

    // Genera datos simulados
    private fun fetchNewData(): List<Gato> {
        return listOf(
            Gato(4, "Nuevo Gato 1", 4.8, "Ciudad D", "Muy sociable"),
            Gato(5, "Nuevo Gato 2", 4.1, "Ciudad E", "Amante de los abrazos")
        )
    }
}
