package com.example.gatoshunter

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.clases.Gato
import com.example.gatoshunter.clases.GatoAdapter
import java.util.concurrent.TimeUnit

class BuscarGato : AppCompatActivity() {

<<<<<<< Updated upstream
    // Base de datos

    // Adaptador para el RecyclerView
    private lateinit var adapter: GatoAdapter

    // Temporizador
//    private val timerDuration = 10 * 60 * 1000L // 10 minutos en milisegundos
//    private lateinit var sharedPreferences: SharedPreferences
=======
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: GatoAdapter
>>>>>>> Stashed changes
    private lateinit var timerTextView: TextView
    private val handler = Handler()
<<<<<<< Updated upstream
//    private var startTime = 0L // Guardar el tiempo de inicio del temporizador
//    private var elapsedTime = 0L // Tiempo transcurrido desde que el temporizador empezó
//    private var remainingTime = timerDuration // Tiempo restante del temporizador
=======
    private var ultimoGatoCompradoId: Int? = null
>>>>>>> Stashed changes

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buscar_gatos)

        val backButton: Button = findViewById(R.id.backbutton)
        val buyButton: Button = findViewById(R.id.buybutton)
<<<<<<< Updated upstream

        //Timer
        timerTextView = findViewById(R.id.Temporizador)
=======
        timerTextView = findViewById(R.id.temporizador)
>>>>>>> Stashed changes

        dbHelper = DatabaseHelper(this)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

<<<<<<< Updated upstream
        // Lista de gatos inicial
        val gatos = listOf(
            Gato(1, "Gato1", 4.5, "Ciudad A", "Gato muy juguetón", "Feliz"),
            Gato(2, "Gato2", 3.2, "Ciudad B", "Gato tranquilo", "Triste"),
            Gato(3, "Gato3", 5.0, "Ciudad C", "Gato curioso", "Encantado")
        )

        // Configuración del adaptador
        adapter = GatoAdapter(gatos)
        recyclerView.adapter = adapter

        // Inicialización de SharedPreferences para el temporizador
//        sharedPreferences = getSharedPreferences("TimerPrefs", MODE_PRIVATE)
//        startTime = sharedPreferences.getLong("startTime", 0L) // Cargar el tiempo de inicio
//        elapsedTime = sharedPreferences.getLong("elapsedTime", 0L) // Tiempo transcurrido
//        remainingTime = timerDuration - elapsedTime // Calcular el tiempo restante

        // Configurar las acciones de los botones
=======
        var data = dbHelper.obtenerGatosLibres()
        if (data != null) {
            data = data.shuffled().take(3)
            guardarGatosMostradosEnPrefs(data.map { it.id!! })
        }

        data = cargarGatosMostradosDePrefs() ?: emptyList()

        adapter = GatoAdapter(data.shuffled().take(3))
        recyclerView.adapter = adapter

>>>>>>> Stashed changes
        backButton.setOnClickListener {
            finish()
        }

        buyButton.setOnClickListener {
<<<<<<< Updated upstream
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
//        updateTimerUI()

    }

    // Método para actualizar la interfaz del temporizador
//    private fun updateTimerUI() {
//        if (remainingTime > 0) {
//            updateTimerText(remainingTime)
//            handler.postDelayed(timerRunnable, 1000) // Actualiza el temporizador cada segundo
//        } else {
//            onTimerFinished()
//        }
//    }

    // Lógica para manejar el final del temporizador
//    private fun onTimerFinished() {
//        findViewById<TextView>(R.id.Temporizador).text = "00:00"
//        sharedPreferences.edit().remove("startTime").apply() // Borra el tiempo de inicio guardado
//        sharedPreferences.edit().remove("elapsedTime").apply() // Borra el tiempo transcurrido guardado
//        updateRecyclerViewData() // Actualiza los datos del RecyclerView con nuevos datos
//        startNewTimer() // Reinicia el temporizador
//    }

    // Reinicia el temporizador guardando el nuevo tiempo de inicio
//    private fun startNewTimer() {
//        startTime = System.currentTimeMillis() // Establece el nuevo tiempo de inicio
//        sharedPreferences.edit().putLong("startTime", startTime).apply() // Guarda el nuevo tiempo de inicio
//        remainingTime = timerDuration // Resetea el tiempo restante
//        updateTimerUI() // Actualiza la interfaz para reflejar el nuevo tiempo
//    }

    // Actualiza el texto del temporizador en la interfaz
//    private fun updateTimerText(remainingTime: Long) {
//        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime) % 60
//        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime) % 60
//        findViewById<TextView>(R.id.Temporizador).text =
//            "Tiempo restante: ${String.format("%02d:%02d", minutes, seconds)}"
//    }

    // Runnable para actualizar el temporizador cada segundo
//    private val timerRunnable = object : Runnable {
//        override fun run() {
//            val currentTime = System.currentTimeMillis()
//            elapsedTime = currentTime - startTime // Calcula el tiempo transcurrido
//            remainingTime = timerDuration - elapsedTime // Calcula el tiempo restante
//
//            if (remainingTime > 0) {
//                updateTimerText(remainingTime)
//                handler.postDelayed(this, 1000) // Actualiza el temporizador en 1 segundo
//            } else {
//                onTimerFinished() // Si el temporizador llega a 0, finaliza
//            }
//        }
//    }

    // Guarda el tiempo de inicio cuando la app se pausa
//    override fun onPause() {
//        super.onPause()
//        handler.removeCallbacks(timerRunnable) // Detiene el Runnable
//        sharedPreferences.edit().putLong("startTime", startTime).apply() // Guarda el tiempo de inicio cuando la app se pausa
//        sharedPreferences.edit().putLong("elapsedTime", elapsedTime).apply() // Guarda el tiempo transcurrido
//    }

    // Calcula el tiempo restante al reanudar la actividad
//    override fun onResume() {
//        super.onResume()
//
//        // Actualizamos inmediatamente el temporizador en la UI para evitar el parpadeo
//        updateTimerUI()
//
//        // Inicia el Runnable para actualizaciones constantes
//        handler.post(timerRunnable)
//    }

    // Simula la actualización de datos del RecyclerView
    private fun updateRecyclerViewData() {
        // Obtén nuevos datos aquí (puede venir de una API o base de datos)
        val newData = fetchNewData()
        adapter.actualizarLista(newData) // Actualiza el adaptador con los nuevos datos
    }

    // Genera datos simulados
    private fun fetchNewData(): List<Gato> {
        return listOf(
            Gato(4, "Nuevo Gato 1", 4.8, "Ciudad D", "Muy sociable", "Encantado"),
            Gato(5, "Nuevo Gato 2", 4.1, "Ciudad E", "Amante de los abrazos", "Triste")
        )
    }
=======
            resolverCompra()
        }
    }

    private fun resolverCompra() {
        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = applicationContext.getAppSharedPreferences()
            val user = prefs.getUserAsync("Usuario")

            if (adapter.selectedItemId != null) {
                val gatoSeleccionado = adapter.getGatoSeleccionado()
                dbHelper.insertarGatoUser(gatoSeleccionado!!, user!!)
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

        val temporizador = TemporizadorMedianoche(timerTextView) {
            updateRecyclerViewData()
        }
        temporizador.iniciar()
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

    private fun updateRecyclerViewData() {
        val newData = dbHelper.obtenerGatosLibres()
        guardarGatosMostradosEnPrefs(newData.map { it.id!! })
        adapter.actualizarLista(newData.shuffled().take(3))
    }

>>>>>>> Stashed changes
    private fun getMillisUntilMidnight(): Long {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
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


<<<<<<< Updated upstream


=======
    private fun cargarGatosMostradosDePrefs(): List<Gato>? {
        val prefs = getSharedPreferences("GatosPrefs", MODE_PRIVATE)
        val idsString = prefs.getString("gatos_ids", null) ?: return null
        val ids = idsString.split(",").mapNotNull { it.toIntOrNull() }
        val newData = dbHelper.obtenerGatosLibres()
        return newData.filter { it.id in ids }
    }
>>>>>>> Stashed changes
}
