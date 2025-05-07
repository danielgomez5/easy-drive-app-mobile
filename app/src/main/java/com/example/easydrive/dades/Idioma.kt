package com.example.easydrive.dades

import com.example.easydrive.R

data class Idioma(
    val idioma:String,
    val foto:Int
)

val listIdiomes = listOf<Idioma>(
    Idioma("Català", R.drawable.catalan),
    Idioma("Español", R.drawable.spanish),
    Idioma("English",R.drawable.english)
)