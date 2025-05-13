package com.example.easydrive.adaptadors

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.easydrive.dades.Cotxe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.Reserva
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException


class AdaptadorRVCotxes(val llista: MutableList<Cotxe>,
                        private val onListChanged: (Boolean) -> Unit,
                        private val documentSelector: ((Cotxe, (File?) -> Unit) -> Unit),
                        private val context: Context) : RecyclerView.Adapter<AdaptadorRVCotxes.ViewHolder>() {
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

        holder.btnEliminar.setOnClickListener {
            AlertDialog.Builder(holder.vista.context)
                .setTitle("Confirmació")
                .setMessage("Estàs segur de voler el·liminar el cotxe definitivament?")
                .setPositiveButton("Sí") { _, _ ->
                    val crud = CrudApiEasyDrive()
                    val matricula = item.matricula

                    if (crud.delCotxe(matricula!!)) {
                        val snackbar = Snackbar.make(
                            holder.vista,
                            "Cotxe el·liminat correctament.",
                            Snackbar.LENGTH_LONG
                        )
                        eliminaCotxe(item)
                        snackbar.setAction("Desfer") {
                            crud.insertCotxe(item)
                            Snackbar.make(holder.vista, "Eliminació cancel·lada. Cotxe restaurat.", Snackbar.LENGTH_SHORT).show()
                            addCotxe(item)
                        }

                        snackbar.show()

                    } else {
                        Snackbar.make(holder.vista, "Tens viatges registrats amb aquest cotxe, el cotxe no s'eliminarà...", Snackbar.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        holder.btnModificar.setOnClickListener {
            val context = holder.vista.context
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_modificar_cotxe, null)
            val dialog = AlertDialog.Builder(context).setView(dialogView).create()

            val etMarca = dialogView.findViewById<EditText>(R.id.etMarca)
            val etModel = dialogView.findViewById<EditText>(R.id.etModel)
            val etAny = dialogView.findViewById<EditText>(R.id.etAny)
            val etTipus = dialogView.findViewById<EditText>(R.id.etTipus)
            val etCapacitat = dialogView.findViewById<EditText>(R.id.etCapacitat)
            val etColor = dialogView.findViewById<EditText>(R.id.etColor)
            val btnAcceptar = dialogView.findViewById<Button>(R.id.btnAcceptar)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
            val btnPujar = dialogView.findViewById<ImageButton>(R.id.btnSeleccionarDoc)

            etMarca.setText(item.marca)
            etModel.setText(item.model)
            etAny.setText(item.any.toString())
            etTipus.setText(item.tipus)
            etCapacitat.setText(item.capacitat.toString())
            etColor.setText(item.color)

            btnPujar.setOnClickListener {
                documentSelector(item) { file ->
                    if (file != null) {
                        item.fotoFitxaTecnica = file.absolutePath
                        Toast.makeText(context, "Fitxa tècnica seleccionada: ${file.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error seleccionant l'arxiu", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnAcceptar.setOnClickListener {
                item.marca = etMarca.text.toString()
                item.model = etModel.text.toString()
                item.any = etAny.text.toString().toIntOrNull()?.toString() ?: item.any
                item.tipus = etTipus.text.toString()
                item.capacitat = etCapacitat.text.toString().toIntOrNull() ?: item.capacitat
                item.color = etColor.text.toString()

                val crud = CrudApiEasyDrive()
                if (crud.updateCotxe(item.matricula!!, item)) {
                    notifyItemChanged(position)

                    if (item.fotoFitxaTecnica!!.isNotEmpty()) {
                        if (crud.updateCotxeFitxaTecnica(item.matricula!!, item.fotoFitxaTecnica.toString())) {
                            Log.d("if insert foto cotxe", "s'ha editat")
                        } else {
                            Log.d("if insert foto cotxe", "NO s'ha editat")
                        }
                    }

                    Snackbar.make(holder.vista, "Cotxe actualitzat correctament.", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(holder.vista, "Error en actualitzar el cotxe.", Snackbar.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }


            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun eliminaCotxe(item: Cotxe) {
        llista.remove(item)
        notifyDataSetChanged()
        onListChanged(llista.isEmpty())
    }

    private fun addCotxe(item: Cotxe) {
        llista.add(item)
        notifyDataSetChanged()
    }
}