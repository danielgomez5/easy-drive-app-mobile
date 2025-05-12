package com.example.easydrive.adaptadors

import com.example.easydrive.dades.Cotxe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.dades.Reserva


class AdaptadorRVCotxes(val llista: MutableList<Cotxe>, private val onListChanged: (Boolean) -> Unit) : RecyclerView.Adapter<AdaptadorRVCotxes.ViewHolder>() {
    class ViewHolder(val vista: View) : RecyclerView.ViewHolder(vista) {
        val tvMatricula: TextView = vista.findViewById(R.id.tvMatricula)
        val tvMarcaModel: TextView = vista.findViewById(R.id.tvMarcaModel)
        val tvAny: TextView = vista.findViewById(R.id.tvAny)
        val tvTipusCapacitat: TextView = vista.findViewById(R.id.tvTipusCapacitat)
        val tvColor: TextView = vista.findViewById(R.id.tvColor)
        val tvHores: TextView = vista.findViewById(R.id.tvHores)
        val btnModificar: Button = vista.findViewById(R.id.btnModificar)
        val btnEliminar: Button = vista.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        return ViewHolder(layout.inflate(R.layout.card_cotxes, parent, false))
    }

    override fun getItemCount(): Int = llista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = llista[position]

        holder.tvMatricula.text = "Matrícula: ${item.matricula}"
        holder.tvMarcaModel.text = "Marca i model: ${item.marca} ${item.model}"
        holder.tvAny.text = "Any: ${item.any}"
        holder.tvTipusCapacitat.text = "Tipus: ${item.tipus} | Capacitat: ${item.capacitat}"
        holder.tvColor.text = "Color: ${item.color}"
        holder.tvHores.text = "Hores conduïdes: ${item.horesTreballades ?: "0"}"

    }

    private fun eliminaCotxe(item: Cotxe) {
        llista.remove(item)
        notifyDataSetChanged()
        onListChanged(llista.isEmpty())
    }
}
