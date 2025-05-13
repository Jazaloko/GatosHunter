package com.example.gatoshunter

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

class Prefs {


}