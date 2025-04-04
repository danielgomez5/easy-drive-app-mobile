package com.example.easydrive.dades

data class Cotxe(
    var matricula: String,
    var marca:String,
    var model:String,
    var any: String,
    var tipus: String,
    var capacitat: Int,
    var color: String,
    var hores_treballades: Float,
    var fitxaTecnica: ByteArray
)
