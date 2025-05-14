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
    private lateinit var userName: EditText
    private lateinit var password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton: Button = findViewById(R.id.buttonLogin)
        val registerButton: Button = findViewById(R.id.buttonRegister)
        val politicaButton: Button = findViewById(R.id.buttonPolitica)
        userName = findViewById(R.id.userName)
        password = findViewById(R.id.password)
        dbHelper = DatabaseHelper(this)

        //Funcion para redirigir a la pagina de Registrar
        registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        val buttonAvisoLegal = findViewById<Button>(R.id.buttonAvisoLegal)
        buttonAvisoLegal.setOnClickListener {
            val intent = Intent(this, AvisoLegalActivity::class.java)
            startActivity(intent)
        }


        // Acción al hacer clic en el botón
        politicaButton.setOnClickListener {
            // Abre la actividad de la política de juego
            val intent = Intent(this, PoliticaLegalActivity::class.java)
            startActivity(intent)
        }


        loginButton.setOnClickListener {
            //Mandar a verificar los datos del login, en caso correcto, redirigir al MainActivity, en caso contrario, Salte mensaje de error
            if (validarCampos()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val (userFound, user) = dbHelper.checkUserAndGetUser(
                        userName.text.toString(),
                        password.text.toString()
                    )

                    withContext(Dispatchers.Main) {
                        if (userFound) {
                            try {
                                val prefs = applicationContext.getAppSharedPreferences()
                                val editor = prefs.edit()
                                prefs.putBooleanAsync("isLoggedIn", true) // Guardar estado de login
                                editor.putUserAsync("Usuario", user!!)

                                Toast.makeText(
                                    this@Login,
                                    "Inicio de sesión exitoso",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this@Login, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } catch (e: Exception) {
                                Log.e("Login", "Error al colocar datos del usuario", e)
                            }

                        } else {
                            Toast.makeText(
                                this@Login,
                                "El usuario no existe o la contraseña es incorrecta",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
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


}