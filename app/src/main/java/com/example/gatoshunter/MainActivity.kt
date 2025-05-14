package com.example.gatoshunter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enlazar vista de imagen de perfil
        profileImageView = findViewById(R.id.profileImageView)

        // Botones funcionales
        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)

        // Acciones de botones
        button1.setOnClickListener {
            openSecondActivity()
        }

        button2.setOnClickListener {
            openThirdActivity()
        }

        // Al pulsar imagen, mostrar opciones
        profileImageView.setOnClickListener {
            showImageSourceDialog()
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
