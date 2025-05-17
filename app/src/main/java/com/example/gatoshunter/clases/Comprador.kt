package com.example.gatoshunter.clases

import java.io.Serializable

data class Comprador(
    var id: Int?,
    var nombre: String,
    var dinero: Double,
    var localidad: String,
    var img: String?
):Serializable