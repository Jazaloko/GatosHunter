package com.example.gatoshunter.clases

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Gato(
    var id: Int?,
    var nombre: String,
    var peso: Double,
    var localidad: String,
    var descripcion: String,
    var emocion: String,
    var img: String?
): Parcelable
