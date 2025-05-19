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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.adaptes.AvatarAdapter
import com.example.gatoshunter.adaptes.GatoAdapter
import com.example.gatoshunter.adaptes.OnGatoDoubleClickListener
import com.example.gatoshunter.clases.*
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
    private lateinit var adapter: GatoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        profileImageView = findViewById(R.id.profileImageView)
        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)
        textUser = findViewById(R.id.textUser)
        textDinero = findViewById(R.id.textDinero)
        dbHelper = DatabaseHelper(this)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter = GatoAdapter(emptyList(), object : OnGatoDoubleClickListener {
            override fun onGatoDoubleClick(gato: Gato) {
                mostrarDialogoGato(gato)
            }
        })

        recyclerView.adapter = adapter

        button1.setOnClickListener {
            startActivity(Intent(this, BuscarGato::class.java))
            finish()
        }

        button2.setOnClickListener {
            startActivity(Intent(this, VenderGato::class.java))
            finish()
        }

        profileImageView.setOnClickListener {
            showImageSourceDialog()
        }

        try {
            colocarDatosUsuario()
            cargarDatosGatos()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al colocar datos del usuario", e)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatosGatos()
    }

    private fun cargarDatosGatos() {
        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = applicationContext.getAppSharedPreferences()
            val user = prefs.getUserAsync("Usuario")!!
            val listaActualizadaDeGatos = dbHelper.obtenerGatosByUser(user)
            Log.e("MainActivity", "Lista de gatos actualizada: $listaActualizadaDeGatos")
            withContext(Dispatchers.Main) {
                adapter.actualizarLista(listaActualizadaDeGatos)
            }
        }
    }

    private fun mostrarDialogoGato(gato: Gato) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_gato, null)

        val imageView = dialogView.findViewById<ImageView>(R.id.dialogGatoImagen)
        val textView = dialogView.findViewById<TextView>(R.id.dialogGatoTexto)

        val resourceId = resources.getIdentifier(gato.img, "drawable", packageName)
        imageView.setImageResource(resourceId) // gato.img es un resourceId
        textView.text = """
        🐱 Nombre: ${gato.nombre}
        🏠 Localidad: ${gato.localidad}
        ⚖️ Peso: ${gato.peso} kg
        😊 Emoción: ${gato.emocion}
        📝 Descripción: ${gato.descripcion}
    """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Información del Gato")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun colocarDatosUsuario() {
        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = applicationContext.getAppSharedPreferences()
            val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
            var user: User? = null

            try {
                user = prefs.getUserAsync("Usuario")
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error loading user data", Toast.LENGTH_SHORT).show()
                }
            }

            if (isLoggedIn && user != null) {
                withContext(Dispatchers.Main) {
                    textUser?.text = user.nombre
                    textDinero?.text = "$" + user.dinero.toString()
                    loadProfileImage(user.img)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Usuario no logeado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadProfileImage(imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            profileImageView.setImageResource(R.drawable.character1)
            return
        }

        if (imagePath.startsWith("drawable/")) {
            val resourceName = imagePath.substring("drawable/".length)
            val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
            if (resourceId != 0) {
                profileImageView.setImageResource(resourceId)
            } else {
                profileImageView.setImageResource(R.drawable.character1)
            }
        } else {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                profileImageView.setImageBitmap(bitmap)
            } else {
                profileImageView.setImageResource(R.drawable.character1)
                Log.w("MainActivity", "Profile image file not found at: $imagePath")
            }
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Elegir de la galería", "Seleccionar avatar predeterminado")

        AlertDialog.Builder(this)
            .setTitle("Selecciona imagen de perfil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openImageChooser()
                    1 -> showAvatarDialog()
                }
            }
            .show()
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun showAvatarDialog() {
        lifecycleScope.launch(Dispatchers.Main) {
            val prefs = applicationContext.getAppSharedPreferences()
            var user = prefs.getUserAsync("Usuario")!!

            val avatarIds = intArrayOf(
                R.drawable.character1, R.drawable.character2, R.drawable.character3,
                R.drawable.character4, R.drawable.character5, R.drawable.character6,
                R.drawable.character7, R.drawable.character8, R.drawable.character9
            )

            val adapter = AvatarAdapter(this@MainActivity, avatarIds)

            AlertDialog.Builder(this@MainActivity)
                .setTitle("Selecciona un avatar")
                .setAdapter(adapter) { _, which ->
                    profileImageView.setImageResource(avatarIds[which])

                    val resourceName = resources.getResourceEntryName(avatarIds[which])
                    if (resourceName != null){
                        val avatarPath = "drawable/$resourceName"
                        user = user.copy(img = avatarPath)
                    }
                    if (user.id != null) {
                        dbHelper.updateUsuario(user)
                    }
                }
                .show()
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                    profileImageView.setImageBitmap(bitmap)
                    val imagePath = saveBitmapToFile(this, bitmap)
                    updateUserProfileImage(imagePath)
                } catch (e: IOException) {
                    Log.e("MainActivity", "Error loading or saving image from gallery", e)
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): String? {
        val fileName = "profile_image_${UUID.randomUUID()}.png"
        val imageFile = File(context.filesDir, fileName)

        try {
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            return imageFile.absolutePath
        } catch (e: IOException) {
            Log.e("MainActivity", "Error saving bitmap to file", e)
            return null
        }
    }

    private fun updateUserProfileImage(newImagePath: String?) {
        val prefs = applicationContext.getAppSharedPreferences()
        var user: User?

        lifecycleScope.launch(Dispatchers.IO) {
            user = prefs.getUserAsync("Usuario")
            user = user?.copy(img = newImagePath)

            try {
                val editor = prefs.edit()
                editor.putUserAsync("Usuario", user!!)
                if (user?.id != null) {
                    dbHelper.updateUsuario(user!!)
                } else {
                    Log.w("MainActivity", "User ID es null")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "No se pudo actualizar la imagen (ID nulo)", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error actualizando imagen en DB o SharedPreferences", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al guardar imagen de perfil", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
