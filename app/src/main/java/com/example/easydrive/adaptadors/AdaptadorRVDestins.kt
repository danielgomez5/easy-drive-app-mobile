package com.example.easydrive.adaptadors

import android.graphics.Color
import android.location.Address
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.dades.GeoapifyDades
import com.example.easydrive.dades.rutaEscollida

class AdaptadorRVDestins(val llista: List<GeoapifyDades>) : RecyclerView.Adapter<AdaptadorRVDestins.ViewHolder>() {
    private var selectedPosition = -1
    class ViewHolder(val vista: View) : RecyclerView.ViewHolder(vista) {
        val textCarrer = vista.findViewById<TextView>(R.id.nomDestiCard)
        val textCiutat = vista.findViewById<TextView>(R.id.ciutatDestiCard)
        val cards = vista.findViewById<CardView>(R.id.card)
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

        if (holder.adapterPosition == selectedPosition) {
            holder.cards.setCardBackgroundColor(Color.GREEN)
        } else {
            holder.cards.setCardBackgroundColor(Color.WHITE)
        }

        holder.vista.setOnClickListener {
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged()
            rutaEscollida = item
        }

    }
}
