package com.example.gatoshunter.clases

import java.io.Serializable

data class Gato(
    var id: Int?,
    var nombre: String,
    var peso: Double,
    var localidad: String,
    var descripcion: String,
    var emocion: String,
    var img: Int?
): Serializable
