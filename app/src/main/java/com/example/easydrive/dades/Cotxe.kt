package com.example.easydrive.dades

import java.io.Serializable

data class Cotxe(
    var matricula: String?,
    var marca:String?,
    var model:String?,
    var any: String?,
    var tipus: String?,
    var capacitat: Int?,
    var color: String?,
    var horesTreballades: Double?,
    var fotoFitxaTecnica: String?
): Serializable