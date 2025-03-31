package com.example.easydrive.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.easydrive.R
import com.example.easydrive.activities.interficie_usuari.MapaRutaUsuari
import com.example.easydrive.databinding.FragmentHomeUsuariBinding

class HomeUsuari : Fragment() {
    private lateinit var binding: FragmentHomeUsuariBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeUsuariBinding.inflate(inflater,container, false)
        binding.btnBuscar.setOnClickListener {
            startActivity(Intent(requireContext(), MapaRutaUsuari::class.java))
        }
        return binding.root
    }

}