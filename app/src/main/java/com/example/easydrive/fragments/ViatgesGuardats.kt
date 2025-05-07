package com.example.easydrive.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easydrive.R
import com.example.easydrive.adaptadors.AdapterViatgesPendents
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.FragmentViatgesGuardatsBinding
import java.time.LocalDate

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

    private fun carregarReserves() {
        val crud = CrudApiEasyDrive()
        val listaViatges: MutableList<Reserva>?=null
        val pendents = crud.getReservesByUsuari(user?.dni!!)
        //val hoy = LocalDate.now()

        pendents?.map { p->
            if (p.idEstat ==2){
                listaViatges?.add(p)
            }
        }
        binding.rcvViatgesPendents.adapter = AdapterViatgesPendents(pendents!!)
        binding.rcvViatgesPendents.layoutManager = LinearLayoutManager(requireContext())
    }

}