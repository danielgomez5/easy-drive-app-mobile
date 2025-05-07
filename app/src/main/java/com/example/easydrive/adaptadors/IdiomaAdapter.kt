package com.example.easydrive.adaptadors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.easydrive.R
import com.example.easydrive.dades.Idioma

class IdiomaAdapter(private val context: Context, private val idiomas: List<Idioma>) :
    ArrayAdapter<Idioma>(context, 0, idiomas) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return crearVista(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return crearVista(position, convertView, parent)
    }

    private fun crearVista(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_idioma, parent, false)
        val idioma = idiomas[position]

        val imgFlag = view.findViewById<ImageView>(R.id.imgFlag)
        val txtLanguage = view.findViewById<TextView>(R.id.txtLanguage)

        imgFlag.setImageResource(idioma.foto)
        txtLanguage.text = idioma.idioma

        return view
    }
}
