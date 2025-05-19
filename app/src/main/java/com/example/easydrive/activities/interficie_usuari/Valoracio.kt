package com.example.easydrive.activities.interficie_usuari

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.easydrive.R
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.Viatja
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityValoracioBinding

class Valoracio : AppCompatActivity() {
    var viatge : Viatja?=null
    var reserva: Reserva?=null
    var taxista: Usuari?=null

    private lateinit var binding: ActivityValoracioBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityValoracioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val crud = CrudApiEasyDrive()
        reserva = intent.getSerializableExtra("reserva") as? Reserva
        viatge = crud.getViatgeByReserva(reserva?.id.toString())
        taxista = crud.getUsuariById(viatge?.idTaxista.toString())
        afegirFoto()
        binding.nomTaxista.setText(taxista?.nom+" "+taxista?.cognom)



        binding.btnValora.setOnClickListener {
            viatge?.comentari = binding.textComentari.text.toString()
            viatge?.valoracio = binding.valoracio.rating
            if (crud.updateViatge(viatge?.id.toString(), viatge!!)){
                Log.d("Update Viatge", "S'ha pogut fer")
                startActivity(Intent(this, IniciUsuari::class.java))
                finish()
            }else{
                Log.d("Update Viatge", "No s'ha pogut fer")
            }
        }
    }

    private fun afegirFoto() {
        try {
            Glide.with(this)
                .load("http://172.16.24.115:7126/Photos/${taxista?.fotoPerfil}")
                .error(R.drawable.logo_easydrive)
                .into(binding.fotoPerfil)
        }catch (e: Exception){
            binding.fotoPerfil.setImageResource(R.drawable.logo_easydrive)
        }
    }
}