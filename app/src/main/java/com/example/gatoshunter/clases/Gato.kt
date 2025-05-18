package com.example.gatoshunter.clases

import kotlinx.serialization.Serializable

@Serializable
data class Gato(
    var id: Int?,
    var nombre: String,
    var peso: Double,
    var localidad: String,
    var descripcion: String,
    var emocion: String,
    var img: String?
)
