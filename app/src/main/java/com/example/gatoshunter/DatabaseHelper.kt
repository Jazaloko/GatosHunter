package com.example.miapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.gatoshunter.R
import com.example.gatoshunter.clases.Comprador
import com.example.gatoshunter.clases.Gato
import com.example.gatoshunter.clases.User

private const val DATABASE_NAME = "Gatos_Hunter.db"
private const val DATABASE_VERSION = 11

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Base de datos
        const val TABLE_GATOS = "Gatos"
        const val TABLE_GATOS_USER = "GatosUser"
        const val TABLE_COMPRADORES = "Compradores"
        const val TABLE_USUARIOS = "Users"
        const val TABLE_COMPRADOR_USER = "CompradorUser"

        // Campos comunes
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "name"
        const val COLUMN_IMG_PATH = "Img_Path"

        // Campos tabla Gatos
        const val COLUMN_PESO = "Peso"
        const val COLUMN_LOCALIDAD = "Localidad"
        const val COLUMN_DESCRIPCION = "Descripcion"
        const val COLUMN_EMOCION = "Emocion"

        // Campos tabla Usuarios
        const val COLUMN_PASSWORD = "pwd"
        const val COLUMN_DINERO = "Dinero"

        // Campos tabla GatosUser
        const val COLUMN_GATO_ID = "idGato"
        const val COLUMN_USER_ID = "idUser"
        const val COLUMN_DATE = "Date"

        // Campos tabla CompradorUser
        const val COLUMN_COMPRADOR_ID = "idComprador"
    }

    // SQL para crear las tablas
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

    private val CREATE_TABLE_COMPRADORES = "CREATE TABLE $TABLE_COMPRADORES (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_NOMBRE TEXT, " +
            "$COLUMN_DINERO REAL, " +
            "$COLUMN_LOCALIDAD TEXT, " +
            "$COLUMN_IMG_PATH TEXT)"

    private val CREATE_TABLE_GATOS_USER = "CREATE TABLE $TABLE_GATOS_USER (" +
            "$COLUMN_GATO_ID INTEGER, " +
            "$COLUMN_USER_ID INTEGER, " +
            "$COLUMN_DATE DATE, " +
            "FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USUARIOS($COLUMN_ID)," +
            "FOREIGN KEY($COLUMN_GATO_ID) REFERENCES $TABLE_GATOS($COLUMN_ID))"

    private val CREATE_TABLE_COMPRADOR_USER = "CREATE TABLE $TABLE_COMPRADOR_USER (" +
            "$COLUMN_COMPRADOR_ID INTEGER, " +
            "$COLUMN_USER_ID INTEGER, " +
            "FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USUARIOS($COLUMN_ID)," +
            "FOREIGN KEY($COLUMN_COMPRADOR_ID) REFERENCES $TABLE_GATOS($COLUMN_ID))"

    override fun onCreate(db: SQLiteDatabase) {
        // Crear las tablas
        db.execSQL(CREATE_TABLE_USUARIOS)
        db.execSQL(CREATE_TABLE_GATOS)
        db.execSQL(CREATE_TABLE_COMPRADORES)
        db.execSQL(CREATE_TABLE_GATOS_USER)
        db.execSQL(CREATE_TABLE_COMPRADOR_USER)

        // Insertar datos iniciales
        insertarDatosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Manejar cambios en la estructura de la base de datos
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GATOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPRADORES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GATOS_USER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPRADOR_USER")
        onCreate(db)
    }

    //region DATOS INICIALES
    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        insertarGatosIniciales(db)
        insertarCompradoresIniciales(db)
    }

    private fun insertarGatosIniciales(db: SQLiteDatabase) {
        val gatosIniciales = listOf(
            Gato(null, "Gato Marron", 4.5, "Ciudad A", "Gato muy juguetón", "Feliz", R.drawable.gato1.toString()),
            Gato(null, "Gato Naranja Claro", 3.2, "Ciudad B", "Gato tranquilo", "Triste", R.drawable.gato2.toString()),
            Gato(null, "Gato Gris Oscuro", 5.0, "Ciudad C", "Gato curioso", "Encantado", R.drawable.gatos3.toString()),
            Gato(null, "Gato Color Piel", 4.8, "Ciudad D", "Muy sociable", "Encantado", R.drawable.gatos4.toString()),
            Gato(null, "Gato Naranja/Negro", 4.1, "Ciudad E", "Amante de los respectivos", "Triste", R.drawable.gato5.toString()),
            Gato(null, "Gato Blanco", 3.5, "Ciudad F", "Le gusta dormir", "Tranquilo", R.drawable.gato6.toString()),
            Gato(null, "Gato Blanco/Negro", 5.2, "Ciudad G", "Un gato amigable", "Feliz", R.drawable.gato7.toString()),
            Gato(null, "Gato Gris Claro", 4.9, "Ciudad H", "Le encanta jugar", "Encantado", R.drawable.gato8.toString()),
            Gato(null, "Gato Naranja Fuerte", 4.3, "Ciudad I", "Le encanta dormir", "Tranquilo", R.drawable.gato9.toString()),
            Gato(null, "Gato Piel/Negro", 4.7, "Ciudad J", "Le encanta comer", "Tranquilo", R.drawable.gato10.toString())
        )

        gatosIniciales.forEach { insertarGato(db, it) }
    }

    private fun insertarCompradoresIniciales(db: SQLiteDatabase) {
        val compradoresIniciales = listOf(
            Comprador(null, "Mercader Errante", 2000.0, "Bosque", R.drawable.character1.toString(), null),
            Comprador(null, "Anciano Sabio", 1500.0, "Montaña", R.drawable.character2.toString(), null),
            Comprador(null, "Pepe", 1000.0, "Desierto", R.drawable.character3.toString(), null),
            Comprador(null, "Juan", 1200.0, "Ciudad", R.drawable.character4.toString(), null),
            Comprador(null, "Maria", 1800.0, "Pueblo", R.drawable.character5.toString(), null),
            Comprador(null, "Luis", 1300.0, "Lago", R.drawable.character6.toString(), null),
            Comprador(null, "Ana", 1600.0, "Playa", R.drawable.character7.toString(), null),
            Comprador(null, "Carlos", 1100.0, "Ciudad", R.drawable.character8.toString(), null),
            Comprador(null, "Laura", 1400.0, "Montaña", R.drawable.character9.toString(), null),
            Comprador(null, "Pedro", 1700.0, "Bosque", R.drawable.character1.toString(), null)
        )
        compradoresIniciales.forEach { insertarComprador(db, it) }
    }

    private fun insertarGato(db: SQLiteDatabase, gato: Gato) {
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, gato.nombre)
            put(COLUMN_PESO, gato.peso)
            put(COLUMN_LOCALIDAD, gato.localidad)
            put(COLUMN_DESCRIPCION, gato.descripcion)
            put(COLUMN_EMOCION, gato.emocion)
            put(COLUMN_IMG_PATH, gato.img)
        }
        db.insert(TABLE_GATOS, null, values)
    }

    private fun insertarComprador(db: SQLiteDatabase, comprador: Comprador) {
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, comprador.nombre)
            put(COLUMN_DINERO, comprador.dinero)
            put(COLUMN_LOCALIDAD, comprador.localidad)
            put(COLUMN_IMG_PATH, comprador.img)
        }
        db.insert(TABLE_COMPRADORES, null, values)
    }

    fun insertarCompradorDiario(compradorId: Int, userId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_COMPRADOR_ID, compradorId)
            put(COLUMN_USER_ID, userId)
        }
        try {
            db.insert(TABLE_COMPRADOR_USER, null, values)
            Log.d("DatabaseHelper", "Insertado comprador $compradorId para usuario $userId en CompradorUser")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al insertar comprador diario: ${e.message}")
        } finally {
            db.close()
        }
    }
    //endregion

    //region INSERTAR DATOS
    fun insertarUsuario(usuario: User) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, usuario.nombre)
            put(COLUMN_DINERO, usuario.dinero)
            put(COLUMN_PASSWORD, usuario.password)
            put(COLUMN_IMG_PATH, usuario.img)
        }
        db.insert(TABLE_USUARIOS, null, values)
        db.close()
    }

    fun insertarGatoUser(gato: Gato, user: User) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_GATO_ID, gato.id)
            put(COLUMN_USER_ID, user.id)
            put(COLUMN_DATE, java.util.Date().toString()) // Puedes usar una forma más específica de obtener la fecha
        }
        db.insert(TABLE_GATOS_USER, null, values)
        db.close()
    }

    fun insertarCompradorUser(comprador: Comprador, user: User) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_COMPRADOR_ID, comprador.id)
            put(COLUMN_USER_ID, user.id)
        }
        db.insert(TABLE_COMPRADOR_USER, null, values)
        db.close()
    }
    //endregion

    //region ELIMINAR DATOS
    // Elimina UN comprador específico de la lista diaria de un usuario
    fun eliminarCompradorDeUsuario(compradorId: Int, userId: Int) {
        val db = writableDatabase
        try {
            val whereClause = "$COLUMN_COMPRADOR_ID = ? AND $COLUMN_USER_ID = ?"
            val whereArgs = arrayOf(compradorId.toString(), userId.toString())
            val deletedRows = db.delete(TABLE_COMPRADOR_USER, whereClause, whereArgs)
            if (deletedRows > 0) {
                Log.d("DatabaseHelper", "Eliminado comprador $compradorId para usuario $userId de CompradorUser. Filas afectadas: $deletedRows")
            } else {
                Log.d("DatabaseHelper", "No se encontró comprador $compradorId para eliminar para usuario $userId en CompradorUser.")
            }

        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al eliminar comprador de usuario: ${e.message}")
        } finally {
            db.close()
        }
    }

    // Elimina TODOS los compradores de la lista diaria de un usuario (para el reinicio diario)
    fun eliminarCompradoresDiariosDeUsuario(usuarioId: Int) {
        val db = writableDatabase
        try {
            val whereClause = "$COLUMN_USER_ID = ?"
            val whereArgs = arrayOf(usuarioId.toString())
            val deletedRows = db.delete(
                TABLE_COMPRADOR_USER,
                whereClause,
                whereArgs
            )
            Log.d("DatabaseHelper", "Eliminados $deletedRows compradores diarios para usuario $usuarioId")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al eliminar compradores diarios de usuario: ${e.message}")
        } finally {
            db.close()
        }
    }

    //Eliminar un gato de la lista de gatos del usuario
    fun eliminarGatoDeUsuario(gatoId: Int, userId: Int) {
        val db = writableDatabase
        try {
            val whereClause = "$COLUMN_GATO_ID = ? AND $COLUMN_USER_ID = ?"
            val whereArgs = arrayOf(gatoId.toString(), userId.toString())
            val deletedRows = db.delete(TABLE_GATOS_USER, whereClause, whereArgs)

            if (deletedRows > 0) {
                Log.d("DatabaseHelper", "Eliminado gato $gatoId para usuario $userId de GatosUser. Filas afectadas: $deletedRows")
                // Puedes realizar acciones adicionales aquí si es necesario
            } else {
                Log.d("DatabaseHelper", "No se encontró gato $gatoId para eliminar para usuario $userId en GatosUser.")
            }
        }
        catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al eliminar gato de usuario: ${e.message}")
        }
        finally {
            db.close()
        }
    }

    //endregion

    //region ACTUALIZAR DATOS
    fun updateUsuario(usuario: User) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, usuario.nombre)
            put(COLUMN_DINERO, usuario.dinero)
            put(COLUMN_PASSWORD, usuario.password)
            put(COLUMN_IMG_PATH, usuario.img)
        }
        val selection = "$COLUMN_ID = ?"
        val selectionArgs = arrayOf(usuario.id.toString())
        db.update(TABLE_USUARIOS, values, selection, selectionArgs)
        db.close()
    }
    //endregion

    //region OBTENER DATOS

    fun getGatoById(id: Int): Gato? {
        val db = readableDatabase
        var gato: Gato? = null
        val cursor = db.query(
            TABLE_GATOS,
            arrayOf(COLUMN_ID, COLUMN_NOMBRE, COLUMN_PESO, COLUMN_LOCALIDAD, COLUMN_DESCRIPCION, COLUMN_EMOCION, COLUMN_IMG_PATH),
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                gato = Gato(
                    it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    it.getDouble(it.getColumnIndexOrThrow(COLUMN_PESO)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_LOCALIDAD)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPCION)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_EMOCION)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_IMG_PATH))
                )
            }
        }
        return gato
    }

    fun getCompradorById(id: Int): Comprador? {
        val db = readableDatabase
        var comprador: Comprador? = null

        val cursor = db.query(
            TABLE_COMPRADORES,
            arrayOf(COLUMN_ID, COLUMN_NOMBRE, COLUMN_DINERO, COLUMN_LOCALIDAD, COLUMN_IMG_PATH),
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                comprador = Comprador(
                    it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    it.getDouble(it.getColumnIndexOrThrow(COLUMN_DINERO)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_LOCALIDAD)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_IMG_PATH)),
                    null
                )
            }
        }
        db.close()
        return comprador

    }

    fun checkUserAndGetUser(userName: String, password: String): Pair<Boolean, User?> {
        val db = readableDatabase
        var user: User?
        val cursor = db.query(
            TABLE_USUARIOS,
            arrayOf(COLUMN_ID, COLUMN_NOMBRE, COLUMN_PASSWORD, COLUMN_DINERO, COLUMN_IMG_PATH),
            "$COLUMN_NOMBRE = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(userName, password),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                user = User(
                    it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                    it.getDouble(it.getColumnIndexOrThrow(COLUMN_DINERO)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_IMG_PATH))
                )
                return Pair(true, user)
            }
        }
        db.close()
        return Pair(false, null)
    }

    fun obtenerGatosByUser(user: User): List<Gato> {
        val db = readableDatabase
        val listaGatos = mutableListOf<Gato>()
        val cursor = db.query(
            TABLE_GATOS_USER,
            arrayOf(COLUMN_GATO_ID),
            "$COLUMN_USER_ID = ?",
            arrayOf(user.id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val idGato = it.getInt(it.getColumnIndexOrThrow(COLUMN_GATO_ID))
                getGatoById(idGato)?.let { gato ->
                    listaGatos.add(gato)
                }
            }
        }
        return listaGatos
    }

    fun obtenerCompradores(): List<Comprador> {
        val db = readableDatabase
        val listaComprador = mutableListOf<Comprador>()
        val cursor = db.query(
            TABLE_COMPRADORES,
            arrayOf(COLUMN_ID, COLUMN_NOMBRE, COLUMN_DINERO, COLUMN_LOCALIDAD, COLUMN_IMG_PATH),
            null,
            null,
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val comprador = Comprador(
                    it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    it.getDouble(it.getColumnIndexOrThrow(COLUMN_DINERO)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_LOCALIDAD)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_IMG_PATH)),
                    null
                )
                listaComprador.add(comprador)
            }
        }
        db.close()
        return listaComprador
    }

    fun obtenerGatos(): List<Gato> {
        val db = readableDatabase
        val listaGatos = mutableListOf<Gato>()
        val cursor = db.query(
            TABLE_GATOS,
            arrayOf(COLUMN_ID, COLUMN_NOMBRE, COLUMN_PESO, COLUMN_LOCALIDAD, COLUMN_DESCRIPCION, COLUMN_EMOCION, COLUMN_IMG_PATH),
            null,
            null,
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val gato = Gato(
                    it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    it.getDouble(it.getColumnIndexOrThrow(COLUMN_PESO)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_LOCALIDAD)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPCION)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_EMOCION)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_IMG_PATH))
                )
                listaGatos.add(gato)
            }
        }
        db.close()
        return listaGatos
    }

    fun obtenerCompradoresDiariosByUser(user: User): List<Comprador> {
        val db = readableDatabase
        val listaCompradores = mutableListOf<Comprador>()
        val cursor = db.query(
            TABLE_COMPRADOR_USER, // Consulta la tabla que usaremos para la lista diaria
            arrayOf(COLUMN_COMPRADOR_ID),
            "$COLUMN_USER_ID = ?",
            arrayOf(user.id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val idComprador = it.getInt(it.getColumnIndexOrThrow(COLUMN_COMPRADOR_ID))
                getCompradorById(idComprador)?.let { comprador ->
                    // NOTA: Aquí el comprador cargado de la tabla principal NO tiene el nombre del gato interesado.
                    // Esa asociación la manejaremos en VenderGato usando SharedPreferences.
                    listaCompradores.add(comprador)
                }
            }
        }
        db.close()
        return listaCompradores
    }
    //endregion
    fun actualizarDineroComprador(compradorId: Int, nuevoDinero: Double) {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("dinero", nuevoDinero)
        }
        val whereClause = "id = ?"
        val whereArgs = arrayOf(compradorId.toString())

        db.update("Compradores", valores, whereClause, whereArgs)
        db.close()
    }
    fun actualizarDineroUsuario(id: Int, dinero: Double) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("Dinero", dinero)
        db.update("Users", values, "id = ?", arrayOf(id.toString())) // <-- CAMBIO AQUÍ
        db.close()
    }
    fun obtenerGatoPorNombre(nombre: String): Gato? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Gatos WHERE name = ?", arrayOf(nombre))

        var gato: Gato? = null
        if (cursor.moveToFirst()) {
            gato = Gato(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                localidad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCALIDAD)),
                peso = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PESO)),
                emocion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMOCION)),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPCION)),
                img = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMG_PATH))
            )
        }

        cursor.close()
        return gato
    }




}