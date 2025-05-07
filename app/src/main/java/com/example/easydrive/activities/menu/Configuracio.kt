package com.example.easydrive.activities.menu

import android.os.Bundle
import android.widget.CompoundButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.adaptadors.IdiomaAdapter
import com.example.easydrive.dades.listIdiomes
import com.example.easydrive.databinding.ActivityConfiguracioBinding

class Configuracio : AppCompatActivity() {
    lateinit var binding : ActivityConfiguracioBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityConfiguracioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("configuracio", MODE_PRIVATE)
        val editor = prefs.edit()

// Leer modo guardado
        val modeOscur = prefs.getBoolean("mode_oscuro", false)
        binding.switchMode.isChecked = modeOscur

// Aplicar modo al iniciar
        AppCompatDelegate.setDefaultNightMode(
            if (modeOscur) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        binding.switchMode.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            // Guardar y aplicar el modo
            editor.putBoolean("mode_oscuro", isChecked)
            editor.apply()

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        val adapter = IdiomaAdapter(this, listIdiomes)
        binding.spinnerIdioma.adapter = adapter


    }
}