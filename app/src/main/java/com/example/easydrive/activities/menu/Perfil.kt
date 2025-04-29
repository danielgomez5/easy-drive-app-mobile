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

        binding.tvBenvinguda.setText("El teu perfil, ${user?.nom}!")

        binding.tieNom.setText(user?.nom)
        binding.tieCognom.setText(user?.cognom)
        binding.tieDataNaix.setText(user?.dataNaixement)
        binding.tieEmail.setText(user?.email)
        binding.tieTelefon.setText(user?.telefon)

    }
}