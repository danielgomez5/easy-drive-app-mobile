package com.example.easydrive.adaptadors

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.dades.Cotxe
import com.example.easydrive.dades.cotxeSeleccionat

class AdaptadorEscollirCotxe (val llista: List<Cotxe>): RecyclerView.Adapter<AdaptadorEscollirCotxe.ViewHolder>(){
    private var selectedPosition = -1
    class ViewHolder(val vista:View): RecyclerView.ViewHolder(vista) {
        val titol = vista.findViewById<TextView>(R.id.textTitol)
        val hores = vista.findViewById<TextView>(R.id.textHores)
        val cards = vista.findViewById<CardView>(R.id.card_escollir_c)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        return ViewHolder(layout.inflate(R.layout.card_cotxe_escollir, parent, false))
    }
    override fun getItemCount(): Int {
        return llista.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int
    ) {
        val item = llista[position]

        holder.titol.setText("${item.matricula} - ${item.marca} ${item.model}")
        val totalMinuts = item.horesTreballades ?: 0f
        val hores = totalMinuts.toInt() / 60
        val minuts = totalMinuts.toInt() % 60

        holder.hores.text = if (hores == 0) {
            "Temps de conducció: %d minuts".format(minuts)
        } else {
            "Temps de conducció: %02d hores i %d minuts".format(hores, minuts)
        }

        if (position == selectedPosition) {
            holder.cards.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.md_theme_secondaryContainer)
            )
        } else {
            holder.cards.setCardBackgroundColor(Color.WHITE)
        }

        holder.vista.setOnClickListener {
            val anterior = selectedPosition
            selectedPosition = holder.adapterPosition

            notifyItemChanged(anterior)
            notifyItemChanged(selectedPosition)

            cotxeSeleccionat = item
        }
    }
}