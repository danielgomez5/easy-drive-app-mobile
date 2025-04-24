package com.example.easydrive.activities.menu

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityPerfilBinding

class Perfil : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.tvNom.text = user?.nom ?: "Nom no disponible"
        binding.tvCognom.text = user?.cognom ?: "Cognom no disponible"
        binding.tvDni.text = user?.dni ?: "DNI no disponible"
        binding.tvEmail.text = user?.email ?: "Email no disponible"
        binding.tvTelefon.text = user?.telefon ?: "Telèfon no disponible"
        binding.tvDataNeix.text = user?.dataNaixement ?: "Data de naixement no disponible"

    }
}