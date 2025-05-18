package com.example.gatoshunter.clases

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comprador(
    var id: Int?,
    var nombre: String,
    var dinero: Double,
    var localidad: String,
    var img: String?,
    var gato: String?
): Parcelable