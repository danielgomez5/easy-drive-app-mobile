package com.example.easydrive.dades

import java.io.Serializable

data class Usuari(
    var dni: String?,
    var nom: String?,
    var cognom: String?,
    var email:String?,
    var telefon: String?,
    var data_neix: String?,
    var passwordHash: String?,
    var foto_perfil: Int?,
    var foto_carnet: Int?,
    var rol: Boolean?,
    var horari: String?,
    var diponibiliat: Boolean?,
    var idZona: Int?
) : Serializable
