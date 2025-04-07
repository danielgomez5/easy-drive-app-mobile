package com.example.easydrive.adaptadors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R

class AdaptadorRVDestins(val llista: List<String>): RecyclerView.Adapter<AdaptadorRVDestins.ViewHolder>() {
    class ViewHolder(val vista: View): RecyclerView.ViewHolder(vista) {
        //val textCarrer = vista.findViewById<TextView>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        return ViewHolder(layout.inflate(R.layout.card_destinsguardats, parent, false))
    }

    override fun getItemCount(): Int {
        return llista.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }
}