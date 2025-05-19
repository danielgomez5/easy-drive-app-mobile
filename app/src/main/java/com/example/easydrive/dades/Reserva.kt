package com.example.easydrive.dades

import java.io.Serializable

data class Reserva(
    val id:Int?,
    var origen: String?,
    var desti: String?,
    var dataReserva : String?,
    var dataViatge : String?,
    var preu : Double?,
    var estat: String?,
    var idUsuari: String?,
    var idEstat: Int?,
    var horaViatge: String?,
    var viewType: Int = 0

): Serializable
