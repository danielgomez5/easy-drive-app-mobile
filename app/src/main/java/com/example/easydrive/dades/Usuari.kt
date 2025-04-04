package com.example.easydrive.dades

import java.io.Serializable
import java.util.Date


data class Usuari(
    var dni: String?,
    var nom: String?,
    var cognom: String?,
    var email:String?,
    var telefon: String?,
    var data_neix: Date?,
    var passwordHash: String?,
    var foto_perfil: ByteArray?,
    var foto_carnet: ByteArray?,
    var rol: Boolean?,
    var horari: String?,
    var diponibiliat: Boolean?,
    var idZona: Int?
) : Serializable
