package com.example.easydrive.dades

data class Cotxe(
    val matricula: String,
    val marca:String,
    val model:String,
    val any: String,
    val tipus: String,
    val capacitat: Int,
    val color: Int,
    val hores_treballades: Float,
    val fitxaTecnica: Int
)
