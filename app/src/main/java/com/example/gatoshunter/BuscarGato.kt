package com.example.gatoshunter

import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
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
import java.util.concurrent.TimeUnit

class BuscarGato : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: GatoAdapter
    private lateinit var timerTextView: TextView
    private val handler = Handler()
    private var ultimoGatoCompradoId: Int? = null

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

        val gatos = dbHelper.obtenerGatosLibres()?.shuffled()?.take(3) ?: emptyList()
        guardarGatosMostradosEnPrefs(gatos.mapNotNull { it.id })

        adapter = GatoAdapter(gatos)
        recyclerView.adapter = adapter

        backButton.setOnClickListener { finish() }

        buyButton.setOnClickListener { resolverCompra() }
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

    private fun updateRecyclerViewData() {
        val nuevos = dbHelper.obtenerGatosLibres()?.shuffled()?.take(3) ?: emptyList()
        guardarGatosMostradosEnPrefs(nuevos.mapNotNull { it.id })
        adapter.actualizarLista(nuevos)
    }

    private fun getMillisUntilMidnight(): Long {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return midnight.timeInMillis - now.timeInMillis
    }

    private fun startMidnightCountdown() {
        handler.post(object : Runnable {
            override fun run() {
                val millisRemaining = getMillisUntilMidnight()
                if (millisRemaining > 0) {
                    updateTimerText(millisRemaining)
                    handler.postDelayed(this, 1000)
                } else {
                    timerTextView.text = "Tiempo restante: 00:00:00"
                    updateRecyclerViewData()
                    handler.postDelayed(this, 1000)
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

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        startMidnightCountdown()
    }
}
