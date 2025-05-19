package com.example.gatoshunter

import android.content.Context
import android.content.SharedPreferences
import com.example.gatoshunter.clases.User
import kotlinx.serialization.json.Json

fun SharedPreferences.getUser(key: String): User? {
    val jsonString = getString(key, null)
    return if (jsonString != null) {
        try {
            Json.decodeFromString(User.serializer(), jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}

fun Context.getCurrentUser(): User? {
    val prefs = this.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    return prefs.getUser("Usuario")
}
