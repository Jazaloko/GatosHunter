package com.example.gatoshunter.clases
import android.graphics.Bitmap
import kotlinx.serialization.Serializable

@Serializable
data class User (
    val id: Int?,
    val nombre: String,
    val password: String,
    val dinero: Double,
    val img: String?
)
