package com.example.easydrive.dades

data class Reserva(
    val id:Int?,
    val origen: String,
    val desti: String,
    val data_reserva : String,
    val data_viatge : String,
    val preu : Double,
    val id_estat: Int
)
