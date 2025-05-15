package com.example.miapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.gatoshunter.clases.Comprador
import com.example.gatoshunter.clases.Gato
import com.example.gatoshunter.clases.User

private const val DATABASE_NAME = "Gatos_Hunter.db"
private const val DATABASE_VERSION = 1

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    //Base de datos, Nombre de las tablas, Campos de las tablas
    companion object {
        const val TABLE_GATOS = "Gatos"
        const val TABLE_COMPRADORES = "Compradores"
        const val TABLE_USUARIOS = "Users"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "name"
        const val COLUMN_PASSWORD = "pwd"
        const val COLUMN_PESO = "Peso"
        const val COLUMN_LOCALIDAD = "Localidad"
        const val COLUMN_DESCRIPCION = "Descripcion"
        const val COLUMN_DINERO = "Dinero"
        const val COLUMN_EMOCION = "Emocion"
    }

    //Secuencia SQL para crear las tablas
    private val CREATE_TABLE_USUARIOS = "CREATE TABLE $TABLE_USUARIOS (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_NOMBRE TEXT UNIQUE, " +
            "$COLUMN_PASSWORD TEXT," +
            "$COLUMN_DINERO REAL)"

    private val CREATE_TABLE_GATOS = "CREATE TABLE $TABLE_GATOS (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_NOMBRE TEXT, " +
            "$COLUMN_PESO REAL, " +
            "$COLUMN_LOCALIDAD TEXT, " +
            "$COLUMN_DESCRIPCION TEXT," +
            "$COLUMN_EMOCION TEXT)"

    private val CREATE_TABLE_COMPRADORES = "CREATE TABLE $TABLE_COMPRADORES (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_NOMBRE TEXT, " +
            "$COLUMN_DINERO REAL, " +
            "$COLUMN_LOCALIDAD TEXT)"

    //Crear tablas
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_USUARIOS)
        db.execSQL(CREATE_TABLE_GATOS)
        db.execSQL(CREATE_TABLE_COMPRADORES)

    }

    //Maneja cambios en la estructura de datos
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GATOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPRADORES")
        onCreate(db)
    }

    //================================= INSERTAR DATOS =========================================

    //Insertar Usuario
    fun insertarUsuario(Usuario: User) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, Usuario.nombre)
            put(COLUMN_DINERO, Usuario.dinero)
            put(COLUMN_PASSWORD, Usuario.password)
        }
        db.insert(TABLE_USUARIOS, null, values)
        db.close()
    }

    //Insertar Gato
    fun insertarGato(Gato: Gato) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, Gato.id)
            put(COLUMN_NOMBRE, Gato.nombre)
            put(COLUMN_PESO, Gato.peso)
            put(COLUMN_LOCALIDAD, Gato.localidad)
            put(COLUMN_DESCRIPCION, Gato.descripcion)
            put(COLUMN_EMOCION, Gato.emocion)
        }
        db.insert(TABLE_GATOS, null, values)
        db.close()
    }

    //Insertar Comprador
    fun insertarComprador(Comprador: Comprador) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, Comprador.id)
            put(COLUMN_NOMBRE, Comprador.nombre)
            put(COLUMN_DINERO, Comprador.dinero)
            put(COLUMN_LOCALIDAD, Comprador.localidad)
        }
        db.insert(TABLE_COMPRADORES, null, values)
        db.close()
    }

    //================================= ELIMINAR DATOS =========================================

    //Eliminar Gato
    fun eliminarGato(id: Int){
        val db = writableDatabase
        db.delete(TABLE_GATOS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    //Eliminar Usuario
    fun eliminarUsuario(id: Int){
        val db = writableDatabase
        db.delete(TABLE_USUARIOS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    //Eliminar Comprador
    fun eliminarComprador(id: Int){
        val db = writableDatabase
        db.delete(TABLE_COMPRADORES, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    //================================= OBTENER DATOS =========================================

    //Devuelve true o false si el usuario existe con el nombre de usuario y la contraseña
    fun checkUserAndGetUser(userName: String, password: String): Pair<Boolean, User?> {

        val db = readableDatabase // Obtiene una instancia de base de datos legible
        val projection = arrayOf(COLUMN_ID, COLUMN_NOMBRE, COLUMN_PASSWORD, COLUMN_DINERO) // Solo necesitamos saber si existe alguna fila
        val selection = "$COLUMN_NOMBRE = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(userName, password)

        val cursor: Cursor? = db.query(
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
            if (it.moveToFirst()){
                val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
                val nombreIndex = it.getColumnIndexOrThrow(COLUMN_NOMBRE)
                val passwordIndex = it.getColumnIndexOrThrow(COLUMN_PASSWORD)
                val dineroIndex = it.getColumnIndexOrThrow(COLUMN_DINERO)

                val id = it.getInt(idIndex)
                val nombre = it.getString(nombreIndex)
                val pwd = it.getString(passwordIndex)
                val dinero = it.getDouble(dineroIndex)

                user = User(id, nombre, pwd, dinero)
                true // Devuelve true si se encontró el usuario
            } else {
                false // Devuelve false si no se encontró
            }
        } ?: false

        cursor?.close() // Cierra el cursor
        db.close() // Cierra la base de datos

        return Pair(userFound, user) // Devuelve true si se encontró al menos una fila, false en caso contrario
    }

    fun actualizarNombreGato(id: Int, nuevoNombre: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", nuevoNombre)
        }
        db.update("GatosUser", values, "idGato = ?", arrayOf(id.toString()))

    }


}
