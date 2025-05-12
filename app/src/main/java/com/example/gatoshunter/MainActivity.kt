package com.example.gatoshunter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enlazar los botones del diseño XML con el código
        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)

        // Configurar acciones para los botones
        button1.setOnClickListener {
            openSecondActivity()
        }

        button2.setOnClickListener {
            openThirdActivity()
        }
    }


    private fun openSecondActivity() {
        val intent = Intent(this, BuscarGato::class.java)
        startActivity(intent)
    }

    private fun openThirdActivity() {
        val intent = Intent(this, VenderGato::class.java)
        startActivity(intent)
    }


}