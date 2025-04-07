package com.example.easydrive.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.easydrive.R
import com.example.easydrive.databinding.FragmentViatgesGuardatsBinding

class ViatgesGuardats : Fragment() {
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
        return binding.root
    }

}