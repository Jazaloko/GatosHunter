package com.example.gatoshunter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val startButton: Button = findViewById(R.id.loginButton)

        startButton.setOnClickListener {
            // Redirigir a login
            try {
                val intent = Intent(this, Login::class.java)
                Log.println(Log.INFO, "Login", "Login button clicked")
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("StartActivity", "Error al iniciar LoginActivity", e)
            }
        }
    }
}