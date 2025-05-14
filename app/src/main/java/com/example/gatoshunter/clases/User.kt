package com.example.gatoshunter.clases
import kotlinx.serialization.Serializable

@Serializable
data class User (
    var id: Int?,
    var nombre: String,
    var password: String,
    var dinero: Double,
)
