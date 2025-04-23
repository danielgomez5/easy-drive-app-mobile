package com.example.easydrive.dades

import java.io.Serializable
import java.util.Date


data class Usuari(
    var dni: String?,
    var nom: String?,
    var cognom: String?,
    var email:String?,
    var telefon: String?,
    var dataNaixement: String?,
    var passwordHash: String?,
    var fotoPerfil: String?,
    var fotoCarnet: String?,
    var rol: Boolean?,
    var horari: String?,
    var diponibiliat: Boolean?,
    var idZona: Int?,
    var matriculas: List<Cotxe>?
) : Serializable

//Variables generals:
var user: Usuari?=null