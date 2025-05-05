package com.example.easydrive.dades

data class Reserva(
    val id:Int?,
    var origen: String?,
    var desti: String?,
    var dataReserva : String?,
    var dataViatge : String?,
    var preu : Double?,
    var idEstat: Int?
)
