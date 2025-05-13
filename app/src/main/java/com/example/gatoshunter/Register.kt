package com.example.gatoshunter

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val registerButton: Button = findViewById(R.id.buttonRegistrar)
        //val volverAtras: Button = findViewById()

        registerButton.setOnClickListener {
            //Comprobar los campos para ver que todos tienen datos, en caso correcto, Registrar al usuario, en caso contrario mostrar mensaje de error
        }
    }
}