package com.example.easydrive.adaptadors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.adaptadors.AdaptadorRVCotxes.ViewHolder
import com.example.easydrive.dades.Step

class AdaptadorIndicacions(val llista: List<Step>): RecyclerView.Adapter<AdaptadorIndicacions.ViewHolder>(){
    class ViewHolder(val vista:View): RecyclerView.ViewHolder(vista) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        return ViewHolder(layout.inflate(R.layout.card_indicacions, parent, false))
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