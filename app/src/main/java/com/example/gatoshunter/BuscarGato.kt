package com.example.gatoshunter

import TemporizadorMedianoche
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buscar_gatos)

        // Inicialización de los botones
        val backButton: Button = findViewById(R.id.backbutton)
        val buyButton: Button = findViewById(R.id.buybutton)

        // Inicialización de la base de datos
        dbHelper = DatabaseHelper(this)

        //Timer
        timerTextView = findViewById(R.id.temporizador)

        // Inicialización del RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        // Configuración del adaptador
        var data = dbHelper.obtenerGatosLibres()

        if (data == null) {
            //Mostrar un mensaje de que ha rescatado a todos los gatos
        } else {
            data = data.shuffled().take(3)
            guardarGatosMostradosEnPrefs(data.map { it.id!! })
        }

        data = cargarGatosMostradosDePrefs()!!
        if (data == null) {
            //Mostrar un mensaje de que ha rescatado a todos los gatos
        }



        adapter = GatoAdapter(data.shuffled().take(3))
        recyclerView.adapter = adapter

        // Configurar las acciones de los botones
        backButton.setOnClickListener {
            finish() // Vuelve a la actividad anterior
        }

        buyButton.setOnClickListener {
            resolverCompra()
        }
    }

    private fun resolverCompra(){
        lifecycleScope.launch(Dispatchers.IO){
            val prefs = applicationContext.getAppSharedPreferences()
            val user = prefs.getUserAsync("Usuario")
            if (adapter.selectedItemId != null) {
                val gatoSeleccionado = adapter.getGatoSeleccionado()

                // Añadir gato a la lista de gatos obtenidos
                dbHelper.insertarGatoUser(gatoSeleccionado!!, user!!)
                dbHelper.eliminarGatoLibre(gatoSeleccionado.id!!)

                // Eliminar el gato de la lista visible
                adapter.eliminarGato(gatoSeleccionado.id!!)
                adapter.selectedItemId = null
            } else {
                Toast.makeText(this@BuscarGato, "Selecciona un gato Primero", Toast.LENGTH_SHORT).show()
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
        val newData = dbHelper.obtenerGatosLibres()
        guardarGatosMostradosEnPrefs(newData.map { it.id!! })
        adapter.actualizarLista(newData.shuffled().take(3)) // Actualiza el adaptador con los nuevos datos
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

        timerTextView.text =
            String.format("Tiempo restante: %02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null) // Detiene el temporizador
    }

    override fun onResume() {
        super.onResume()
        startMidnightCountdown() // Lo reinicia al volver
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
        val newData = dbHelper.obtenerGatosLibres()
        return newData.filter { it.id in ids }
    }




}
