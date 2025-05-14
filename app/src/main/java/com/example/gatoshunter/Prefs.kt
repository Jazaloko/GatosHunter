package com.example.gatoshunter

import android.content.Context
import android.content.SharedPreferences
import com.example.gatoshunter.clases.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.lang.reflect.Array

// Nombre del archivo de SharedPreferences
private const val PREFS_NAME = "MyAppPreferences"

// Función de extensión para obtener SharedPreferences
fun Context.getAppSharedPreferences(): SharedPreferences {
    return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

// Funciones de extensión para operaciones asíncronas de SharedPreferences

suspend fun SharedPreferences.putStringAsync(key: String, value: String) =
    withContext(Dispatchers.IO) {
        edit().putString(key, value).apply() // apply() es asíncrono
    }

suspend fun SharedPreferences.getStringAsync(key: String, defaultValue: String? = null): String? =
    withContext(Dispatchers.IO) {
        getString(key, defaultValue)
    }

suspend fun SharedPreferences.putIntAsync(key: String, value: Int) =
    withContext(Dispatchers.IO) {
        edit().putInt(key, value).apply()
    }

suspend fun SharedPreferences.getIntAsync(key: String, defaultValue: Int = 0): Int =
    withContext(Dispatchers.IO) {
        Array.getInt(key, defaultValue)
    }

suspend fun SharedPreferences.putBooleanAsync(key: String, value: Boolean) =
    withContext(Dispatchers.IO) {
        edit().putBoolean(key, value).apply()
    }

suspend fun SharedPreferences.getBooleanAsync(key: String, defaultValue: Boolean = false): Boolean =
    withContext(Dispatchers.IO) {
        getBoolean(key, defaultValue)
    }

// Función de extensión para guardar un objeto User de forma asíncrona
suspend fun SharedPreferences.Editor.putUserAsync(key: String, user: User) =
    withContext(Dispatchers.IO) {
        val jsonString = Json.encodeToString(user)
        putString(key, jsonString) // Guarda la cadena JSON en SharedPreferences
        apply() // Aplica los cambios de forma asíncrona
    }

// Función de extensión para recuperar un objeto User de forma asíncrona
suspend fun SharedPreferences.getUserAsync(key: String): User? =
    withContext(Dispatchers.IO) {
        val jsonString = getString(key, null) // Recupera la cadena JSON
        if (jsonString != null) {
            try {
                Json.decodeFromString<User>(jsonString) // Deserializa la cadena JSON a un objeto User
            } catch (e: Exception) {
                // Manejar errores de deserialización (por ejemplo, si el formato JSON es incorrecto)
                e.printStackTrace()
                null // Devolver null si hay un error al deserializar
            }
        } else {
            null // Devolver null si la clave no existe o el valor es null
        }
    }

class Prefs {


}