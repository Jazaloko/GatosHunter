package com.example.gatoshunter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.clases.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private val PICK_IMAGE_REQUEST = 1
    var textUser: TextView? = null
    var textDinero: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enlazar vista de imagen de perfil
        profileImageView = findViewById(R.id.profileImageView)

        // Botones funcionales
        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)
        textUser = findViewById(R.id.textUser)
        textDinero = findViewById(R.id.textDinero)



        // Acciones de botones
        button1.setOnClickListener {
            val intent = Intent(this, BuscarGato::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(this, VenderGato::class.java)
            startActivity(intent)
        }

        try {
            colocarDatosUsuario()
        } catch (e : Exception) {
            Log.e("MainActivity", "Error al colocar datos del usuario", e)
        }

    }

    fun colocarDatosUsuario() {

        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = applicationContext.getAppSharedPreferences()
            val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
            var user: User? = null // Assuming User is the type returned by getUserAsync

            try {
                user = prefs.getUserAsync("Usuario")
            } catch (e: Exception) {
                // Handle the error, e.g., log it or show an error message
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error loading user data", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            if (isLoggedIn && user != null) {
                withContext(Dispatchers.Main) {
                    textUser?.text = user.nombre
                    textDinero?.text = "$${user.dinero}"
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Usuario no logeado", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    // Diálogo para elegir entre galería o avatares
    private fun showImageSourceDialog() {
        val options = arrayOf("Elegir de la galería", "Seleccionar avatar predeterminado")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona imagen de perfil")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openImageChooser()
                1 -> showAvatarDialog()
            }
        }
        builder.show()
    }

    // Galería del teléfono
    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Selección de imágenes del drawable
    private fun showAvatarDialog() {
        val avatarIds = intArrayOf(
            R.drawable.character1, R.drawable.character2, R.drawable.character3,
            R.drawable.character4, R.drawable.character5, R.drawable.character6,
            R.drawable.character7, R.drawable.character8, R.drawable.character9
        )

        val adapter = AvatarAdapter(this, avatarIds)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona un avatar")
        builder.setAdapter(adapter) { _, which ->
            profileImageView.setImageResource(avatarIds[which])
        }
        builder.show()
    }

    // Resultado de la galería
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                profileImageView.setImageURI(selectedImageUri)
            }
        }
    }
}