package com.example.easydrive.adaptadors

import android.location.Address
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.dades.GeoapifyDades

class AdaptadorRVDestins(val llista: List<GeoapifyDades>) : RecyclerView.Adapter<AdaptadorRVDestins.ViewHolder>() {

    class ViewHolder(val vista: View) : RecyclerView.ViewHolder(vista) {
        val textCarrer = vista.findViewById<TextView>(R.id.nomDestiCard)
        val textCiutat = vista.findViewById<TextView>(R.id.ciutatDestiCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        return ViewHolder(layout.inflate(R.layout.card_nomdestins, parent, false))
    }

    override fun getItemCount(): Int = llista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = llista[position]
        holder.textCarrer.setText(item.address_line1.toString() + ",")
        holder.textCiutat.setText(item.city.toString())
    }
}
