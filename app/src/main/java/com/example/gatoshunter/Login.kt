package com.example.gatoshunter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton: Button = findViewById(R.id.buttonLogin)
        val registerButton: Button = findViewById(R.id.buttonRegister)
        val userName: EditText = findViewById(R.id.userName)
        val password: EditText = findViewById(R.id.password)

        //Funcion para redirigir a la pagina de Registrar
        registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            if (userName.text.isNullOrEmpty() || userName.text.isNullOrBlank() && password.text.isNullOrBlank() || userName.text.isNullOrEmpty()){
                //Mostrar un mensaje de error por que faltan datos
            }
            //Mandar a verificar los datos del login, en caso correcto, redirigir al MainActivity, en caso contrario, Salte mensaje de error
        }
    }

}