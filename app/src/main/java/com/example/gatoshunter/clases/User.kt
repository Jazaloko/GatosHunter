package com.example.gatoshunter.clases

import java.io.Serializable

data class User(
    val id: Int?,
    val nombre: String,
    val password: String,
    val dinero: Double,
    val img: String?
):Serializable
