package com.example.gatoshunter

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gatoshunter.clases.User
import com.example.miapp.database.DatabaseHelper

class Register : AppCompatActivity() {

    // Declara las variables para tus vistas
    private lateinit var userName: EditText
    private lateinit var password: EditText
    private lateinit var registerButton: Button

    // private lateinit var volverAtras: Button // Si tienes un botón de volver atrás
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userName = findViewById(R.id.userName)
        password = findViewById(R.id.password)
        registerButton = findViewById(R.id.buttonRegistrar)
        //volverAtras = findViewById()
        dbHelper = DatabaseHelper(this)

        registerButton.setOnClickListener {
            //Comprobar los campos para ver que todos tienen datos, en caso correcto, Registrar al usuario, en caso contrario mostrar mensaje de error
            // Llama a la función de validación
            try {
                if (validarCampos()) {
                    // Si la validación es exitosa, procede con el registro
                    registrarUsuario()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("Register", "Error al registrar", e)
            }

        }


    }

    private fun validarCampos(): Boolean {
        var esValido = false

        // Validar ambos campos
        val userNameEmpty = userName.text.isNullOrEmpty() || userName.text.isNullOrBlank()
        val passwordEmpty = password.text.isNullOrEmpty() || password.text.isNullOrBlank()

        if (userNameEmpty && passwordEmpty) {
            // Ambos campos están vacíos
            Toast.makeText(
                this,
                "Por favor, rellena el nombre de usuario y la contraseña.",
                Toast.LENGTH_SHORT
            ).show()
        } else if (userNameEmpty) {
            Toast.makeText(this, "Por favor, rellena el nombre de usuario.", Toast.LENGTH_SHORT)
                .show()
            esValido = false
        } else if (passwordEmpty) {
            Toast.makeText(this, "Por favor, rellena la contraseña.", Toast.LENGTH_SHORT).show()
            esValido = false
        } else {
            esValido = true
        }

        return esValido // Devuelve true si todos los campos son válidos
    }

    // Función para manejar la lógica de registro del usuario
    private fun registrarUsuario() {
        // Obtén los valores de los campos de entrada
        val userName = userName.text.toString().trim()
        val password = password.text.toString().trim()

        val usuario = User(null, userName, password, 100.0, null)
        dbHelper.insertarUsuario(usuario)

        // Ejemplo simple: mostrar un Toast de éxito
        Toast.makeText(this, "Usuario $userName registrado exitosamente.", Toast.LENGTH_SHORT)
            .show()
    }


}