package com.example.gatoshunter
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PoliticaLegalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_politica_de_juego)

        // Botón para volver
        val botonVolver = findViewById<Button>(R.id.buttonVolverPolitica)
        botonVolver.setOnClickListener {
            finish()  // Cierra esta pantalla y regresa a la anterior
        }
    }
}
