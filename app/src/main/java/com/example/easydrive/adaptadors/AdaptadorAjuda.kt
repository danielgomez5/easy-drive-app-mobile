package com.example.easydrive.adaptadors

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.activities.menu.Contacte
import com.example.easydrive.dades.AjudaDades
import com.google.android.material.bottomsheet.BottomSheetBehavior

class AdaptadorAjuda (val llista: List<AjudaDades>,
                      val layout_bottom_sheet: ConstraintLayout,
                      val bottom_behavior: BottomSheetBehavior<ConstraintLayout>,
    ): RecyclerView.Adapter<AdaptadorAjuda.ViewHolder>(){
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
        holder.pregunta.setText(item.pregunta)

        holder.btn.setOnClickListener {
            bottom_behavior.state = BottomSheetBehavior.STATE_EXPANDED
            layout_bottom_sheet.findViewById<TextView>(R.id.titol).setText(item.pregunta)
            layout_bottom_sheet.findViewById<TextView>(R.id.textAjuda).setText(item.resposta)
        }

        layout_bottom_sheet.findViewById<Button>(R.id.btnContacte).setOnClickListener {
            bottom_behavior.state = BottomSheetBehavior.STATE_HIDDEN
            val intent = Intent(holder.vista.context, Contacte::class.java)
            holder.vista.context.startActivity(intent)
        }

    }


}