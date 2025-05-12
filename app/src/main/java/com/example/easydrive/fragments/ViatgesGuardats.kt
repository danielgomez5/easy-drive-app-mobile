package com.example.easydrive.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easydrive.R
import com.example.easydrive.adaptadors.AdapterViatgesPendents
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.FragmentViatgesGuardatsBinding
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class ViatgesGuardats : Fragment() { //Al final son reserves/viatges pendents
    private lateinit var binding: FragmentViatgesGuardatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViatgesGuardatsBinding.inflate(inflater,container,false)
        carregarReserves()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carregarReserves()

        if (arguments?.getBoolean("MOSTRAR_SNACKBAR") == true) {
            Snackbar.make(
                view,
                "Reserva realitzada correctament!",
                Snackbar.LENGTH_LONG
            ).setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.md_theme_secondary))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onSecondary))
                .show()
        }
    }

    private fun carregarReserves() {
        val crud = CrudApiEasyDrive()
        val listaViatges = mutableListOf<Reserva>()
        val pendents = crud.getReservesByUsuari(user?.dni!!)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = sdf.parse(sdf.format(Date()))

        Log.d("Lista de pendents", pendents.toString())
        pendents?.forEach { p ->
            val dataReserva = p.dataViatge?.let { sdf.parse(it) }
            if ((p.idEstat == 2 || p.idEstat == 1) && dataReserva != null && !dataReserva.before(hoy)) {
                listaViatges.add(p)
            }
        }
        Log.d("carregar reserva", listaViatges.toString())


        if (listaViatges.isNullOrEmpty()){
            binding.llEmptyState.visibility = View.VISIBLE
        }else{
            binding.llEmptyState.visibility = View.GONE
            binding.rcvViatgesPendents.adapter = AdapterViatgesPendents(listaViatges) { isEmpty ->
                binding.llEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            }
            binding.rcvViatgesPendents.layoutManager = LinearLayoutManager(requireContext())
        }

    }
}