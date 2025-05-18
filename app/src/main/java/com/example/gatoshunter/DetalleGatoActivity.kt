package com.example.gatoshunter

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gatoshunter.clases.Gato

class DetalleGatoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GATO = "extra_gato"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_gato)

        val gato = intent.getSerializableExtra(EXTRA_GATO) as? Gato

        val textNombre = findViewById<TextView>(R.id.textNombre)
        val textLocalidad = findViewById<TextView>(R.id.textLocalidad)
        val textPeso = findViewById<TextView>(R.id.textPeso)
        val textEmocion = findViewById<TextView>(R.id.textEmocion)
        val textDescripcion = findViewById<TextView>(R.id.textDescripcion)
        val imageGato = findViewById<ImageView>(R.id.imgGato)

        if (gato != null) {

            textNombre.text = gato.nombre
            textLocalidad.text = gato.localidad
            textPeso.text = "${gato.peso} kg"
            textEmocion.text = gato.emocion
            textDescripcion.text = gato.descripcion
            gato.img?.let {
                imageGato.setImageResource(it)
            }
        }
    }
}
