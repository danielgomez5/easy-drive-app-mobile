package com.example.easydrive.dades

data class Reserva(
    val id:Int?,
    var origen: String,
    var desti: String,
    var data_reserva : String,
    var data_viatge : String,
    var preu : Double,
    var id_estat: Int
)
