package com.example.easydrive.activities.registre

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.activities.interficie_usuari.IniciUsuari
import com.example.easydrive.databinding.ActivityRegistre3Binding

class Registre3 : AppCompatActivity() {
    private lateinit var binding : ActivityRegistre3Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistre3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.imagebtnR1.setOnClickListener {
            startActivity(Intent(this, Registre2::class.java))
            finish()
        }

        binding.crearCompte.setOnClickListener {
            startActivity(Intent(this, IniciUsuari::class.java))
            finish()
        }
    }
}