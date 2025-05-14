package com.example.gatoshunter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.clases.AvatarAdapter
import com.example.gatoshunter.clases.Gato
import com.example.gatoshunter.clases.GatoAdapter
import com.example.gatoshunter.clases.MainAdapter
import com.example.gatoshunter.clases.User
import com.example.miapp.database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var dbHelper: DatabaseHelper
    var textUser: TextView? = null
    var textDinero: TextView? = null
    private lateinit var adapter: MainAdapter

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
        dbHelper = DatabaseHelper(this)

        // Inicialización del RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        adapter = MainAdapter(emptyList())
        recyclerView.adapter = adapter

        // Acciones de botones
        button1.setOnClickListener {
            val intent = Intent(this, BuscarGato::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(this, VenderGato::class.java)
            startActivity(intent)
        }

        profileImageView.setOnClickListener {
            showImageSourceDialog()

        }

        try {
            colocarDatosUsuario()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al colocar datos del usuario", e)
        }

    }

    override fun onResume() {
        super.onResume()
        // Recargar los datos de los gatos cada vez que la Activity vuelve a estar visible
        cargarDatosGatos()
    }

    private fun cargarDatosGatos() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Asume que tienes una función en DatabaseHelper para obtener todos los gatos
            val listaActualizadaDeGatos = dbHelper.obtenerGatosUser()
            withContext(Dispatchers.Main) {
                // Actualiza la lista en el adaptador y notifica los cambios
                adapter.actualizarLista(listaActualizadaDeGatos)
            }
        }
    }

    fun colocarDatosUsuario() {

        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = applicationContext.getAppSharedPreferences()
            val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
            var user: User? = null

            try {
                user = prefs.getUserAsync("Usuario")
            } catch (e: Exception) {
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
                    loadProfileImage(user.img)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Usuario no logeado", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }


    }

    // Load the profile image into the ImageView
    private fun loadProfileImage(imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            // No image path stored, set a default image
            profileImageView.setImageResource(R.drawable.character1) // Replace with your default
            return
        }

        // Check if the path is for a drawable resource (e.g., "drawable/character1")
        if (imagePath.startsWith("drawable/")) {
            val resourceName = imagePath.substring("drawable/".length)
            val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
            if (resourceId != 0) {
                profileImageView.setImageResource(resourceId)
            } else {
                // Resource not found, set default
                profileImageView.setImageResource(R.drawable.character1)
            }
        } else {
            // Assume it's a file path
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                profileImageView.setImageBitmap(bitmap)
            } else {
                // File not found, set default and potentially clear the path in SharedPreferences/DB
                profileImageView.setImageResource(R.drawable.character1)
                Log.w("MainActivity", "Profile image file not found at: $imagePath")
                // You might want to clear the invalid path here
                // updateUserProfileImage(null)
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
                try {
                    // Cargar el Bitmap desde la Uri
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)

                    // Mostrar la imagen seleccionada en el ImageView
                    profileImageView.setImageBitmap(bitmap)

                    // Guardar el Bitmap en un archivo y obtener la ruta
                    val imagePath = saveBitmapToFile(this, bitmap)

                    // Actualizar la información del usuario con la nueva ruta de la imagen
                    updateUserProfileImage(imagePath)

                } catch (e: IOException) {
                    Log.e("MainActivity", "Error loading or saving image from gallery", e)
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Función auxiliar para guardar un Bitmap en un archivo y devolver la ruta
    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): String? {
        val fileName = "profile_image_${UUID.randomUUID()}.png" // Nombre de archivo único
        val imageFile = File(context.filesDir, fileName) // Guardar en almacenamiento interno

        try {
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos) // Comprimir y guardar
            fos.close()
            return imageFile.absolutePath // Devolver la ruta absoluta del archivo
        } catch (e: IOException) {
            Log.e("MainActivity", "Error saving bitmap to file", e)
            return null
        }
    }

    // Función para actualizar la ruta de la imagen de perfil del usuario
    private fun updateUserProfileImage(newImagePath: String?) {

        val prefs = applicationContext.getAppSharedPreferences()
        var user: User?

        // Actualizar en SharedPreferences y en la base de datos (en un Coroutine)
        lifecycleScope.launch(Dispatchers.IO) {
            user = prefs.getUserAsync("Usuario")

            // Creamos una copia del usuario con la nueva ruta de imagen
            user = user?.copy(img = newImagePath)

            try {

                //Actualizar en SharedPreferences
                val editor = prefs.edit()
                editor.putUserAsync(
                    "Usuario",
                    user!!
                ) // Usamos !! aquí porque ya verificamos que currentUser no es null

                //Actualizar en la base de datos SQLite
                if (user?.id != null) {
                    dbHelper.updateUsuario(user!!)
                } else {
                    Log.w("MainActivity", "User ID es null")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "No se pudo actualizar la imagen en la base de datos (ID nulo)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "MainActivity",
                    "Error actualizando la imagen en la base de datos o en SharedPreferences",
                    e
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al guardar la imagen de perfil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}