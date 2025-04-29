package com.example.easydrive.dades

data class DadesPagament(
    val id: Int?,
    var numero_tarjeta: String,
    var titular:String,
    var data_expiracio: String,
    var id_usuari: String
)
