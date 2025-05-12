package com.example.gatoshunter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton: Button = findViewById("x")
        val registerButton: Button = findViewById("x")

        //Funcion para redirigir a la pagina de Registrar
        registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            //Mandar a verificar los datos del login, en caso correcto, redirigir al MainActivity, en caso contrario, Salte mensaje de error
        }
    }

}