package com.example.miapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.fragment.app.add
import com.example.gatoshunter.clases.Comprador
import com.example.gatoshunter.clases.Gato
import com.example.gatoshunter.clases.User

private const val DATABASE_NAME = "Gatos_Hunter.db"
private const val DATABASE_VERSION = 6

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    //Base de datos, Nombre de las tablas, Campos de las tablas
    companion object {
        const val TABLE_GATOS = "Gatos"
        const val TABLE_GATOS_USER = "GatosUser"
        const val TABLE_GATOS_LIBRES = "GatosLibres"
        const val TABLE_COMPRADORES = "Compradores"
        const val TABLE_USUARIOS = "Users"
        const val COLUMN_ID = "id"
        const val COLUMN_USER_ID = "idUser"
        const val COLUMN_NOMBRE = "name"
        const val COLUMN_PASSWORD = "pwd"
        const val COLUMN_PESO = "Peso"
        const val COLUMN_LOCALIDAD = "Localidad"
        const val COLUMN_DESCRIPCION = "Descripcion"
        const val COLUMN_DINERO = "Dinero"
        const val COLUMN_EMOCION = "Emocion"
        const val COLUMN_IMG_PATH = "Img_Path"
    }

    //Secuencia SQL para crear las tablas
    private val CREATE_TABLE_USUARIOS = "CREATE TABLE $TABLE_USUARIOS (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_NOMBRE TEXT UNIQUE, " +
            "$COLUMN_PASSWORD TEXT," +
            "$COLUMN_DINERO REAL, " +
            "$COLUMN_IMG_PATH TEXT)"

    private val CREATE_TABLE_GATOS = "CREATE TABLE $TABLE_GATOS (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_NOMBRE TEXT, " +
            "$COLUMN_PESO REAL, " +
            "$COLUMN_LOCALIDAD TEXT, " +
            "$COLUMN_DESCRIPCION TEXT," +
            "$COLUMN_EMOCION TEXT, " +
            "$COLUMN_IMG_PATH TEXT)"

    private val CREATE_TABLE_GATOS_LIBRE = "CREATE TABLE $TABLE_GATOS_LIBRES (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_NOMBRE TEXT, " +
            "$COLUMN_PESO REAL, " +
            "$COLUMN_LOCALIDAD TEXT, " +
            "$COLUMN_DESCRIPCION TEXT," +
            "$COLUMN_EMOCION TEXT, " +
            "$COLUMN_IMG_PATH TEXT)"

    private val CREATE_TABLE_GATOS_USER = "CREATE TABLE $TABLE_GATOS_USER (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_NOMBRE TEXT, " +
            "$COLUMN_PESO REAL, " +
            "$COLUMN_LOCALIDAD TEXT, " +
            "$COLUMN_DESCRIPCION TEXT," +
            "$COLUMN_EMOCION TEXT, " +
            "$COLUMN_IMG_PATH TEXT, " +
            "$COLUMN_USER_ID INTEGER, " +
            "FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USUARIOS($COLUMN_ID))"

    private val CREATE_TABLE_COMPRADORES = "CREATE TABLE $TABLE_COMPRADORES (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_NOMBRE TEXT, " +
            "$COLUMN_DINERO REAL, " +
            "$COLUMN_LOCALIDAD TEXT, " +
            "$COLUMN_IMG_PATH TEXT)"

    //Crear tablas
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_USUARIOS)
        db.execSQL(CREATE_TABLE_GATOS)
        db.execSQL(CREATE_TABLE_COMPRADORES)
        db.execSQL(CREATE_TABLE_GATOS_USER)
        db.execSQL(CREATE_TABLE_GATOS_LIBRE)

        insertarDatosIniciales(db)

    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        // Insertar Usuarios iniciales
        val Gato1 = Gato(null, "Gato1", 4.5, "Ciudad A", "Gato muy juguetón", "Feliz", null) // ID nulo para autoincremento
        val Gato2 = Gato(null, "Gato2", 3.2, "Ciudad B", "Gato tranquilo", "Triste", null)
        val Gato3 = Gato(null, "Gato3", 5.0, "Ciudad C", "Gato curioso", "Encantado", null)
        val Gato4 = Gato(null, "Gato4", 4.8, "Ciudad D", "Muy sociable", "Encantado", null)
        val Gato5 = Gato(null, "Gato5", 4.1, "Ciudad E", "Amante de los respectivos", "Triste", null)
        val Gato6 = Gato(null, "Gato6", 3.5, "Ciudad F", "Le gusta dormir", "Tranquilo", null)
        val Gato7 = Gato(null, "Gato7", 5.2, "Ciudad G", "Un gato amigable", "Feliz", null)
        val Gato8 = Gato(null, "Gato8", 4.9, "Ciudad H", "Le encanta jugar", "Encantado", null)
        val Gato9 = Gato(null, "Gato9", 4.3, "Ciudad I", "Le encanta dormir", "Tranquilo", null)
        val Gato10 = Gato(null, "Gato10", 4.7, "Ciudad J", "Le encanta comer", "Tranquilo", null)

        insertarGatoInicial(db, Gato1)
        insertarGatoInicial(db, Gato2)
        insertarGatoInicial(db, Gato3)
        insertarGatoInicial(db, Gato4)
        insertarGatoInicial(db, Gato5)
        insertarGatoInicial(db, Gato6)
        insertarGatoInicial(db, Gato7)
        insertarGatoInicial(db, Gato8)
        insertarGatoInicial(db, Gato9)
        insertarGatoInicial(db, Gato10)

        // Insertar Compradores iniciales
        val comprador1 = Comprador(null, "Mercader Errante", 2000.0, "Bosque", null)
        val comprador2 = Comprador(null, "Anciano Sabio", 1500.0, "Montaña", null)

        insertarCompradorInicial(db, comprador1)
        insertarCompradorInicial(db, comprador2)
    }

    // Funciones auxiliares para insertar datos en onCreate (usan la instancia de db proporcionada)
    private fun insertarGatoInicial(db: SQLiteDatabase, gato: Gato) {
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, gato.nombre)
            put(COLUMN_PESO, gato.peso)
            put(COLUMN_LOCALIDAD, gato.localidad)
            put(COLUMN_DESCRIPCION, gato.descripcion)
            put(COLUMN_EMOCION, gato.emocion)
            put(COLUMN_IMG_PATH, gato.img)
        }
        db.insert(TABLE_GATOS, null, values)
        db.insert(TABLE_GATOS_LIBRES, null, values)
    }

    private fun insertarCompradorInicial(db: SQLiteDatabase, comprador: Comprador) {
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, comprador.nombre)
            put(COLUMN_DINERO, comprador.dinero)
            put(COLUMN_LOCALIDAD, comprador.localidad)
            put(COLUMN_IMG_PATH, comprador.img)
        }
        db.insert(TABLE_COMPRADORES, null, values)
    }

    //Maneja cambios en la estructura de datos
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GATOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPRADORES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GATOS_USER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GATOS_LIBRES")
        onCreate(db)
    }

    //================================= INSERTAR DATOS =========================================

    //Insertar Usuario
    fun insertarUsuario(usuario: User) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, usuario.nombre)
            put(COLUMN_DINERO, usuario.dinero)
            put(COLUMN_PASSWORD, usuario.password)
            put(COLUMN_IMG_PATH, usuario.img)
        }
        db.insert(TABLE_USUARIOS, null, values)
        db.close()
    }

    //Insertar Gato_User
    fun insertarGatoUser(Gato: Gato, User: User) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, Gato.id)
            put(COLUMN_NOMBRE, Gato.nombre)
            put(COLUMN_PESO, Gato.peso)
            put(COLUMN_LOCALIDAD, Gato.localidad)
            put(COLUMN_DESCRIPCION, Gato.descripcion)
            put(COLUMN_EMOCION, Gato.emocion)
            put(COLUMN_IMG_PATH, Gato.img)
            put(COLUMN_USER_ID, User.id)
        }
        db.insert(TABLE_GATOS_USER, null, values)
        db.close()
    }

    //================================= ELIMINAR DATOS =========================================

    //Eliminar Gato de la tabla Libre
    fun eliminarGatoLibre(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_GATOS_LIBRES, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    //================================= ACTUALIZAR DATOS =========================================

    fun updateUsuario(usuario: User) {
        val db =
            this.writableDatabase // Obtiene una instancia de la base de datos en modo escritura
        val values = ContentValues() // Crea un objeto ContentValues para los valores a actualizar

        // Agrega los valores que quieres actualizar
        // No incluyas el ID aquí, ya que se usa en la cláusula WHERE
        values.put(COLUMN_NOMBRE, usuario.nombre)
        values.put(COLUMN_DINERO, usuario.dinero)
        values.put(COLUMN_PASSWORD, usuario.password) // Considera la seguridad
        values.put(COLUMN_IMG_PATH, usuario.img) // Actualiza la ruta de la imagen

        // Define la cláusula WHERE para especificar qué fila(s) actualizar
        val selection = "$COLUMN_ID = ?" // Selecciona la fila donde el ID coincide
        val selectionArgs =
            arrayOf(usuario.id.toString()) // Argumentos para la cláusula WHERE (el ID del usuario)

        // Realiza la actualización
        // db.update(nombreTabla, valoresAActualizar, clausulaWHERE, argumentosWHERE)
        val count = db.update(
            TABLE_USUARIOS,
            values,
            selection,
            selectionArgs
        )

        db.close() // Cierra la conexión a la base de datos
    }

    //================================= OBTENER DATOS =========================================

    //Devuelve true o false si el usuario existe con el nombre de usuario y la contraseña
    fun checkUserAndGetUser(userName: String, password: String): Pair<Boolean, User?> {

        val db = readableDatabase // Obtiene una instancia de base de datos legible
        val projection = arrayOf(
            COLUMN_ID,
            COLUMN_NOMBRE,
            COLUMN_PASSWORD,
            COLUMN_DINERO,
            COLUMN_IMG_PATH
        ) // Solo necesitamos saber si existe alguna fila
        val selection = "$COLUMN_NOMBRE = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(userName, password)

        val cursor: Cursor = db.query(
            TABLE_USUARIOS,   // La tabla a consultar
            projection,       // Las columnas a devolver (solo el ID es suficiente)
            selection,        // Las columnas para la cláusula WHERE
            selectionArgs,    // Los valores para la cláusula WHERE
            null,     // No agrupar las filas
            null,      // No filtrar por grupos de filas
            null      // El orden de clasificación
        )

        var user: User? = null
        val userFound = cursor?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
                val nombreIndex = it.getColumnIndexOrThrow(COLUMN_NOMBRE)
                val passwordIndex = it.getColumnIndexOrThrow(COLUMN_PASSWORD)
                val dineroIndex = it.getColumnIndexOrThrow(COLUMN_DINERO)
                val imgIndex = it.getColumnIndexOrThrow(COLUMN_IMG_PATH)

                val id = it.getInt(idIndex)
                val nombre = it.getString(nombreIndex)
                val pwd = it.getString(passwordIndex)
                val dinero = it.getDouble(dineroIndex)
                val img = it.getString(imgIndex)

                user = User(id, nombre, pwd, dinero, img)
                true // Devuelve true si se encontró el usuario
            } else {
                false // Devuelve false si no se encontró
            }
        } ?: false

        cursor?.close() // Cierra el cursor
        db.close() // Cierra la base de datos

        return Pair(
            userFound,
            user
        ) // Devuelve true si se encontró al menos una fila, false en caso contrario
    }

    fun obtenerGatosUser(): List<Gato>{

        val db = readableDatabase
        val listaGatos = mutableListOf<Gato>()

        val projection = arrayOf(
            COLUMN_ID,
            COLUMN_NOMBRE,
            COLUMN_PESO,
            COLUMN_EMOCION,
            COLUMN_IMG_PATH,
            COLUMN_LOCALIDAD,
            COLUMN_DESCRIPCION
        )

        val cursor: Cursor = db.query(
            TABLE_GATOS_USER,   // La tabla a consultar
            projection,       // Las columnas a devolver
            null,        // Las columnas para la cláusula WHERE
            null,    // Los valores para la cláusula WHERE
            null,     // No agrupar las filas
            null,      // No filtrar por grupos de filas
            null      // El orden de clasificación
        )

        // Procesar el Cursor
        cursor.use { // Usa 'use' para asegurar que el cursor se cierre automáticamente
            // Obtener los índices de las columnas
            val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
            val nombreIndex = it.getColumnIndexOrThrow(COLUMN_NOMBRE)
            val pesoIndex = it.getColumnIndexOrThrow(COLUMN_PESO)
            val localidadIndex = it.getColumnIndexOrThrow(COLUMN_LOCALIDAD)
            val descripcionIndex = it.getColumnIndexOrThrow(COLUMN_DESCRIPCION)
            val emocionIndex = it.getColumnIndexOrThrow(COLUMN_EMOCION)
            val imgPathIndex = it.getColumnIndexOrThrow(COLUMN_IMG_PATH)

            // Iterar sobre las filas del cursor
            while (it.moveToNext()) {
                // Leer los datos de la fila actual usando los índices de las columnas
                val id = it.getInt(idIndex)
                val nombre = it.getString(nombreIndex)
                val peso = it.getDouble(pesoIndex)
                val localidad = it.getString(localidadIndex)
                val descripcion = it.getString(descripcionIndex)
                val emocion = it.getString(emocionIndex)
                val imgPath = it.getString(imgPathIndex) // Puede ser null si la columna permite nulls

                // Crear un objeto Gato con los datos de la fila
                val gato = Gato(id, nombre, peso, localidad, descripcion, emocion, imgPath)

                // Agregar el objeto Gato a la lista
                listaGatos.add(gato)
            }
        }

        db.close() // Cierra la conexión a la base de datos

        return listaGatos

    }

    fun obtenerGatosLibres(): List<Gato>{

        val db = readableDatabase
        val listaGatos = mutableListOf<Gato>()

        val projection = arrayOf(
            COLUMN_ID,
            COLUMN_NOMBRE,
            COLUMN_PESO,
            COLUMN_EMOCION,
            COLUMN_IMG_PATH,
            COLUMN_LOCALIDAD,
            COLUMN_DESCRIPCION
        )

        val cursor: Cursor = db.query(
            TABLE_GATOS_LIBRES,   // La tabla a consultar
            projection,       // Las columnas a devolver
            null,        // Las columnas para la cláusula WHERE
            null,    // Los valores para la cláusula WHERE
            null,     // No agrupar las filas
            null,      // No filtrar por grupos de filas
            null      // El orden de clasificación
        )

        // Procesar el Cursor
        cursor.use { // Usa 'use' para asegurar que el cursor se cierre automáticamente
            // Obtener los índices de las columnas
            val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
            val nombreIndex = it.getColumnIndexOrThrow(COLUMN_NOMBRE)
            val pesoIndex = it.getColumnIndexOrThrow(COLUMN_PESO)
            val localidadIndex = it.getColumnIndexOrThrow(COLUMN_LOCALIDAD)
            val descripcionIndex = it.getColumnIndexOrThrow(COLUMN_DESCRIPCION)
            val emocionIndex = it.getColumnIndexOrThrow(COLUMN_EMOCION)
            val imgPathIndex = it.getColumnIndexOrThrow(COLUMN_IMG_PATH)

            // Iterar sobre las filas del cursor
            while (it.moveToNext()) {
                // Leer los datos de la fila actual usando los índices de las columnas
                val id = it.getInt(idIndex)
                val nombre = it.getString(nombreIndex)
                val peso = it.getDouble(pesoIndex)
                val localidad = it.getString(localidadIndex)
                val descripcion = it.getString(descripcionIndex)
                val emocion = it.getString(emocionIndex)
                val imgPath = it.getString(imgPathIndex) // Puede ser null si la columna permite nulls

                // Crear un objeto Gato con los datos de la fila
                val gato = Gato(id, nombre, peso, localidad, descripcion, emocion, imgPath)

                // Agregar el objeto Gato a la lista
                listaGatos.add(gato)
            }
        }

        db.close() // Cierra la conexión a la base de datos

        return listaGatos

    }

    fun actualizarNombreGato(id: Int, nuevoNombre: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, nuevoNombre)
        }
        db.update(TABLE_GATOS_USER, values, "id = ?", arrayOf(id.toString()))

    }


}
