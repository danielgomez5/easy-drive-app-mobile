package com.example.easydrive.adaptadors

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.api.geoapify.CrudGeo
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.rutaDesti
import com.example.easydrive.dades.rutaEscollida
import com.example.easydrive.dades.rutaOrigen
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AdapterViatgesPendents(val llista: MutableList<Reserva>, private val onListChanged: (Boolean) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class CompactViewHolder(val vista: View) : RecyclerView.ViewHolder(vista) {
        val destiFinal = vista.findViewById<TextView>(R.id.NomDesti)
        val cancel = vista.findViewById<ImageButton>(R.id.btn_borrar)
    }

    class ExpandedViewHolder(val vista: View) : RecyclerView.ViewHolder(vista) {
        val textDesti = vista.findViewById<TextView>(R.id.textDesti)
        val origen = vista.findViewById<TextView>(R.id.textOrigen)
        val dataViatge = vista.findViewById<TextView>(R.id.textDataViatge)
        val hora = vista.findViewById<TextView>(R.id.textHora)
        val preu = vista.findViewById<TextView>(R.id.textPreu)
        val cancel = vista.findViewById<ImageButton>(R.id.btnBorrar)
    }

    override fun getItemViewType(position: Int): Int {
        return llista[position].viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            val view = inflater.inflate(R.layout.card_viatgespendents, parent, false)
            CompactViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.card_viatgespendents_expand, parent, false)
            ExpandedViewHolder(view)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = llista[position]
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfEuro = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaFormateada = sdfEuro.format(sdf.parse(item.dataViatge))
        val horaFormateada = try {
            LocalTime.parse(item.horaViatge).format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            item.horaViatge ?: ""
        }


        when (holder) {
            is CompactViewHolder -> {
                holder.destiFinal.text = item.desti
                holder.cancel.setOnClickListener {
                    AlertDialog.Builder(holder.vista.context)
                        .setTitle("Confirmació")
                        .setMessage("Estàs segur que vols cancel·lar aquesta reserva?")
                        .setPositiveButton("Sí") { _, _ ->
                            val crud = CrudApiEasyDrive()
                            item.idEstat = 4
                            if (crud.changeEstatReserva(item.id.toString(), item)) {
                                cancelarReserva(item)
                                Toast.makeText(holder.vista.context, "S'ha cancel·lat la reserva correctament", Toast.LENGTH_LONG).show()
                            }
                        }
                        .setNegativeButton("No", null)
                        .show()
                }

                holder.vista.setOnClickListener {
                    llista.forEach { it.viewType = 0 }
                    llista[position].viewType = 1
                    notifyDataSetChanged()
                }
            }


            is ExpandedViewHolder -> {
                holder.textDesti.text = "${item.desti}"
                holder.origen.text = "Origen: ${item.origen}"
                holder.dataViatge.text = "Data de viatge: $fechaFormateada"
                holder.hora.text = "Hora: ${horaFormateada}"
                holder.preu.text = "Preu: ${String.format("%.2f €", item.preu ?: 0.0)}"

                holder.cancel.setOnClickListener {
                    AlertDialog.Builder(holder.vista.context)
                        .setTitle("Confirmació")
                        .setMessage("Estàs segur que vols cancel·lar aquesta reserva?")
                        .setPositiveButton("Sí") { _, _ ->
                            val crud = CrudApiEasyDrive()
                            item.idEstat = 4
                            if (crud.changeEstatReserva(item.id.toString(), item)) {
                                cancelarReserva(item)
                                Toast.makeText(holder.vista.context, "S'ha cancel·lat la reserva correctament", Toast.LENGTH_LONG).show()
                            }
                        }
                        .setNegativeButton("No", null)
                        .show()
                }


                holder.vista.setOnClickListener {
                    llista.forEach { it.viewType = 0 }
                    llista[position].viewType = 0
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount(): Int = llista.size

    private fun cancelarReserva(item: Reserva) {
        llista.remove(item)
        notifyDataSetChanged()
        onListChanged(llista.isEmpty())
    }
}