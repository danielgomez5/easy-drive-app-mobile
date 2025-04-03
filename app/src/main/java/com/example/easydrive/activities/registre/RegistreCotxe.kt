package com.example.easydrive.activities.registre

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.activities.MainActivity
import com.example.easydrive.dades.Usuari
import com.example.easydrive.databinding.ActivityRegistreCotxeBinding

class RegistreCotxe : AppCompatActivity() {
    private lateinit var binding : ActivityRegistreCotxeBinding
    private var usuari: Usuari?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistreCotxeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.imagebtnR1.setOnClickListener {
            startActivity(Intent(this, Registre2::class.java))
        }

        binding.btnSeguent.setOnClickListener {
            startActivity(Intent(this, Registre3::class.java))
        }
    }
}