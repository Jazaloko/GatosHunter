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

class VenderGato : AppCompatActivity() {

    // Adaptador para el RecyclerView
    private lateinit var adapter: CompradorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vender_gatos)

        // Inicialización de los botones
        val backButton: Button = findViewById(R.id.backbutton)
        val sellButton: Button = findViewById(R.id.sellbutton)

        // Inicialización del RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        //recyclerView.setHasFixedSize(true)

        // Lista de gatos inicial
        val compradores = listOf(
            Comprador(1, "Paco", "Las palmas"),
            Comprador(2, "Pepe", "Telde")
        )

        // Configuración del adaptador
        adapter = CompradorAdapter(compradores)
        recyclerView.adapter = adapter


        // Configurar las acciones de los botones
        backButton.setOnClickListener {
            finish() // Vuelve a la actividad anterior
        }

        sellButton.setOnClickListener {
            if (adapter.selectedItemId != null) {
                val idSeleccionado = adapter.selectedItemId!!

                // Eliminar el gato de la lista visible
                adapter.eliminarComprador(idSeleccionado)
                adapter.selectedItemId = null
            } else {
                Toast.makeText(this, "Selecciona un comprador primero", Toast.LENGTH_SHORT).show()
            }
        }

    }

}
