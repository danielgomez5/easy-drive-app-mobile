package com.example.easydrive.dades

data class Usuari(
    val dni: String,
    val nom: String,
    val cognom: String,
    val email:String,
    val telefon: String,
    val data_neix: String,
    val passwordHash: String,
    val foto_perfil: Int,
    val foto_carnet: Int,
    val rol: Int,
    val horari: String,
    val diponibiliat: Boolean?
)
