package com.example.gatoshunter

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AvisoLegalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aviso_legal)
        val volverButton = findViewById<Button>(R.id.buttonVolver)

        // Al hacer clic, cerrar esta pantalla y volver atrás
        volverButton.setOnClickListener {
            finish()  // Esto vuelve al LoginActivity si fue quien abrió esta Activity
        }
    }
}