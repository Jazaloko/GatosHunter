package com.example.miapp.database

import android.content.ClipDescription
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.gatoshunter.Gato

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    //Crear tabla
    override fun onCreate(db: SQLiteDatabase) {
        // Crear la tabla "gatos"
        db.execSQL(
            "CREATE TABLE $TABLE_GATOS (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_NOMBRE TEXT, " +
                    "$COLUMN_PESO REAL, " +
                    "$COLUMN_LOCALIDAD TEXT, " +
                    "$COLUMN_DESCRIPCION TEXT) "
        )

        //Crear la tabla "Clientes"
        db.execSQL(
            "CREATE TABLE $TABLE_CLIENTES (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_NOMBRE TEXT, " +
                    "$COLUMN_DINERO REAL, " +
                    "$COLUMN_LOCALIDAD TEXT, " +
                    "$COLUMN_DESCRIPCION TEXT, " +
                    "$COLUMN_GATOS TEXT)"
        )
    }

    //Maneja cambios en la estructura de datos
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GATOS")
        onCreate(db)
    }

    //Base de datos, Nombre de la tabla, Campos de la tabla
    companion object {
        const val DATABASE_NAME = "Gatos_Hunter.db"
        const val DATABASE_VERSION = 1

        const val TABLE_GATOS = "Gatos"
        const val TABLE_CLIENTES = "Clientes"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_PESO = "Peso"
        const val COLUMN_LOCALIDAD = "Localidad"
        const val COLUMN_DESCRIPCION = "Descripcion"
        const val COLUMN_DINERO = "Dinero"
        const val COLUMN_GATOS = "Gatos"
    }

    fun insertarGato(gato: Gato) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, gato.id)
            put(COLUMN_NOMBRE, gato.nombre)
            put(COLUMN_PESO, gato.peso)
            put(COLUMN_LOCALIDAD, gato.localidad)
            put(COLUMN_DESCRIPCION, gato.descripcion)

        }
        db.insert(TABLE_GATOS, null, values)
        db.close()
    }

    fun obtenerGatos(): List<Gato> {
        val listaGatos = mutableListOf<Gato>()
        val db = readableDatabase
        val query = db.query(TABLE_GATOS, null, null, null, null, null, null)

        if (query.moveToFirst()) {
            do {
                val gato = Gato(
                    id = query.getInt(query.getColumnIndexOrThrow(COLUMN_ID)),
                    nombre = query.getString(query.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    peso = query.getDouble(query.getColumnIndexOrThrow(COLUMN_PESO)),
                    localidad = query.getString(query.getColumnIndexOrThrow(COLUMN_LOCALIDAD)),
                    descripcion = query.getString(query.getColumnIndexOrThrow(COLUMN_DESCRIPCION))
                )
                listaGatos.add(gato)
            } while (query.moveToNext())
        }
        query.close()
        return listaGatos
    }

    fun eliminarGato(id: Int){
        val db = writableDatabase
        db.delete(TABLE_GATOS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }




}
