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
    private lateinit var adapter: MainAdapter

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

        adapter = MainAdapter(emptyList(), object : OnGatoDoubleClickListener {
            override fun onGatoDoubleClick(gato: Gato) {
                mostrarDialogoGato(gato)
            }
        })

        recyclerView.adapter = adapter

        button1.setOnClickListener {
            startActivity(Intent(this, BuscarGato::class.java))
        }

        button2.setOnClickListener {
            startActivity(Intent(this, VenderGato::class.java))
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
        cargarDatosGatos()
    }

    private fun cargarDatosGatos() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listaActualizadaDeGatos = dbHelper.obtenerGatosUser()
            withContext(Dispatchers.Main) {
                adapter.actualizarLista(listaActualizadaDeGatos)
            }
        }
    }

    private fun mostrarDialogoGato(gato: Gato) {
        val mensaje = """
            ${gato.img}
            🐱 Nombre: ${gato.nombre}
            🏠 Localidad: ${gato.localidad}
            ⚖️ Peso: ${gato.peso} kg
            😊 Emoción: ${gato.emocion}
            📝 Descripción: ${gato.descripcion}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Información del Gato")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
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
                    Toast.makeText(this@MainActivity, "Error loading user data", Toast.LENGTH_SHORT).show()
                }
            }

            if (isLoggedIn && user != null) {
                withContext(Dispatchers.Main) {
                    textUser?.text = user.nombre
                    textDinero?.text = "$${user.dinero}"
                    loadProfileImage(user.img)  // Aquí img es Int
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Usuario no logeado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadProfileImage(imageResId: Int?) {
        if (imageResId == null || imageResId == 0) {
            profileImageView.setImageResource(R.drawable.character1) // imagen por defecto
            return
        }

        profileImageView.setImageResource(imageResId)
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
        val avatarIds = intArrayOf(
            R.drawable.character1, R.drawable.character2, R.drawable.character3,
            R.drawable.character4, R.drawable.character5, R.drawable.character6,
            R.drawable.character7, R.drawable.character8, R.drawable.character9
        )

        val adapter = AvatarAdapter(this, avatarIds)

        AlertDialog.Builder(this)
            .setTitle("Selecciona un avatar")
            .setAdapter(adapter) { _, which ->
                profileImageView.setImageResource(avatarIds[which])
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // En vez de cargar imagen de galería, asignamos un drawable fijo
            val drawableId = R.drawable.character1 // Ejemplo: drawable fijo
            profileImageView.setImageResource(drawableId)
            updateUserProfileImage(drawableId)
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

    private fun updateUserProfileImage(newImageResId: Int?) {
        val prefs = applicationContext.getAppSharedPreferences()
        var user: User?

        lifecycleScope.launch(Dispatchers.IO) {
            user = prefs.getUserAsync("Usuario")
            user = user?.copy(img = newImageResId)

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
