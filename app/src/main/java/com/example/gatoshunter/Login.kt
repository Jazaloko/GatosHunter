package com.example.gatoshunter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.miapp.database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Login : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton: Button = findViewById(R.id.buttonLogin)
        val registerButton: Button = findViewById(R.id.buttonRegister)
        val userName: EditText = findViewById(R.id.userName)
        val password: EditText = findViewById(R.id.password)
        dbHelper = DatabaseHelper(this)

        Log.d("Login", "onCreate called")

        //Funcion para redirigir a la pagina de Registrar
        registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            //Mandar a verificar los datos del login, en caso correcto, redirigir al MainActivity, en caso contrario, Salte mensaje de error
            if (userName.text.isNullOrEmpty() || userName.text.isNullOrBlank() && password.text.isNullOrBlank() || userName.text.isNullOrEmpty()){
                //Mostrar un mensaje de error por que faltan datos
            } else {
                lifecycleScope.launch(Dispatchers.IO){
                    val (userFound, user) = dbHelper.checkUserAndGetUser(userName.text.toString(), password.text.toString())

                    withContext(Dispatchers.Main) {
                        if (userFound) {
                            try {
                                val prefs = applicationContext.getAppSharedPreferences()
                                val editor = prefs.edit()
                                prefs.putBooleanAsync("isLoggedIn", true) // Guardar estado de login
                                editor.putUserAsync("Usuario", user!!)

                                Toast.makeText(this@Login, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@Login, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } catch (e : Exception) {
                                Log.e("Login", "Error al colocar datos del usuario", e)
                            }

                        } else {
                            // Mostrar un mensaje de error
                        }
                    }
                }
            }
        }
    }



}