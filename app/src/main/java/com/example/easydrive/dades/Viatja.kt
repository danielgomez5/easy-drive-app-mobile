package com.example.easydrive.dades

data class Viatja(
    var id: Int?,
    var durada: Int?,
    var distancia: Double?,
    var valoracio: Float?,
    var comentari: String?,
    var idZona: Int?,
    var idTaxista: String?,
    var idReserva: Int?,
    var idCotxe: String?,
    var idReservaNavigation: Reserva?
)
