package com.example.easydrive.adaptadors

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.dades.Viatja
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AdaptadorViatges(val llista: List<Viatja>): RecyclerView.Adapter<AdaptadorViatges.ViewHolder>() {
    class ViewHolder(vista:View): RecyclerView.ViewHolder(vista) {
        val tvRuta = vista.findViewById<TextView>(R.id.tvRuta)
        val tvData = vista.findViewById<TextView>(R.id.tvData)
        val tvDistanciaDurada = vista.findViewById<TextView>(R.id.tvDistanciaDurada)
        val tvValoracio = vista.findViewById<TextView>(R.id.tvValoracio)
        val tvComentari = vista.findViewById<TextView>(R.id.tvComentari)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        return ViewHolder(layout.inflate(R.layout.card_historial_viatges, parent, false))
    }
    override fun getItemCount(): Int {
        return llista.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int
    ) {
        val viatge = llista[position]

        val reserva = viatge.idReservaNavigation
        val origen = reserva?.origen ?: "Origen desconegut"
        val desti = reserva?.desti ?: "Destí desconegut"
        val dataViatge = reserva?.dataViatge ?: "Data no disponible"

        holder.tvRuta.text = "$origen → $desti"
        val entrada = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val sortida = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        val dataFormatejada = LocalDate.parse(dataViatge, entrada).format(sortida)

        holder.tvData.text = dataFormatejada

        holder.tvDistanciaDurada.text = "Distància: ${viatge.distancia} km · Durada: ${viatge.durada} min"
        holder.tvValoracio.text = "⭐ ${viatge.valoracio ?: "-"}"

        if (!viatge.comentari.isNullOrEmpty()) {
            holder.tvComentari.visibility = View.VISIBLE
            holder.tvComentari.text = "Comentari: ${viatge.comentari}"
        } else {
            holder.tvComentari.visibility = View.GONE
        }
    }
}