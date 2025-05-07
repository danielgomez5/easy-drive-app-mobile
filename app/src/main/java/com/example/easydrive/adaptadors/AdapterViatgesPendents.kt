package com.example.easydrive.adaptadors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.dades.Reserva

class AdapterViatgesPendents(val llista: MutableList<Reserva>): RecyclerView.Adapter<AdapterViatgesPendents.ViewHolder>() {
    class ViewHolder(val vista: View): RecyclerView.ViewHolder(vista) {
        val destiFinal = vista.findViewById<TextView>(R.id.NomDesti)
        val cancel = vista.findViewById<Button>(R.id.btn_cancel)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        return ViewHolder(layout.inflate(R.layout.card_viatgespendents, parent, false))
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = llista[position]
        holder.destiFinal.setText(item.desti)
        holder.cancel.setOnClickListener {

        }

    }

    override fun getItemCount(): Int {
        return llista.size
    }


}