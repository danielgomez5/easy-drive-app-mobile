package com.example.easydrive.activities.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easydrive.R
import com.example.easydrive.activities.interficie_usuari.IniciUsuari
import com.example.easydrive.adaptadors.AdaptadorViatges
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.Viatja
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityHistorialViatgesBinding

class HistorialViatges : AppCompatActivity() {
    lateinit var binding: ActivityHistorialViatgesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistorialViatgesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.imagebtnR1.setOnClickListener {
            finish()
        }

        carregarViatges()
    }

    private fun carregarViatges() {
        val crud = CrudApiEasyDrive()
        var viatges : List<Viatja>? = null


        try {
            if (user?.rol == false){
                viatges = crud.getAllViatgesByUsuari(user?.dni!!)
            }else{
                viatges = crud.getAllViatgesByTaxista(user?.dni!!)
            }

            if (!viatges.isNullOrEmpty()) {
                mostrarViatges(viatges)
            } else {
                mostrarEmptyState()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mostrarEmptyState()
        }
    }

    private fun mostrarEmptyState() {
        binding.llEmptyState.visibility = View.VISIBLE
        binding.rcvViatgesPendents.visibility = View.GONE


        if (user?.rol == false){
            binding.makeAbook.visibility = View.VISIBLE
            binding.makeAbook.setOnClickListener {
                val intent = Intent(this, IniciUsuari::class.java)
                startActivity(intent)
            }
        }else{
            binding.makeAbook.visibility = View.GONE
        }

    }

    private fun mostrarViatges(viatges: List<Viatja>) {
        binding.rcvViatgesPendents.adapter = AdaptadorViatges(viatges)
        binding.rcvViatgesPendents.layoutManager = LinearLayoutManager(this@HistorialViatges)
        binding.rcvViatgesPendents.visibility = View.VISIBLE

        binding.llEmptyState.visibility = View.GONE
    }
}