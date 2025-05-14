package com.example.gatoshunter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.clases.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    var textUser: TextView? = null
    var textDinero: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enlazar los botones del diseño XML con el código
        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)
        textUser = findViewById(R.id.textUser)
        textDinero = findViewById(R.id.textDinero)



        // Configurar acciones para los botones
        button1.setOnClickListener {
            val intent = Intent(this, BuscarGato::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(this, VenderGato::class.java)
            startActivity(intent)
        }

        try {
            colocarDatosUsuario()
        } catch (e : Exception) {
            Log.e("MainActivity", "Error al colocar datos del usuario", e)
        }

    }

    fun colocarDatosUsuario() {

        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = applicationContext.getAppSharedPreferences()
            val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
            var user: User? = null // Assuming User is the type returned by getUserAsync

            try {
                user = prefs.getUserAsync("Usuario")
            } catch (e: Exception) {
                // Handle the error, e.g., log it or show an error message
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error loading user data", Toast.LENGTH_SHORT).show()
                }
            }

            if (isLoggedIn && user != null) {
                withContext(Dispatchers.Main) {
                    textUser?.text = user.nombre
                    textDinero?.text = "$${user.dinero}"
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Usuario no logeado", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}