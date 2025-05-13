package com.example.easydrive.adaptadors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.dades.AjudaDades

class AdaptadorAjuda (val llista: List<AjudaDades>): RecyclerView.Adapter<AdaptadorAjuda.ViewHolder>(){
    class ViewHolder(val vista:View): RecyclerView.ViewHolder(vista) {
        val pregunta = vista.findViewById<TextView>(R.id.pregunta)
        val btn = vista.findViewById<ImageView>(R.id.btnObrirSheetAjuda)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        return ViewHolder(layout.inflate(R.layout.card_ajuda, parent, false))
    }
    override fun getItemCount(): Int {
        return llista.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = llista[position]


    }


}