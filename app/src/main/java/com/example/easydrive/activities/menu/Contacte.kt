package com.example.easydrive.activities.menu

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.activities.interficie_taxista.IniciTaxista
import com.example.easydrive.activities.interficie_usuari.IniciUsuari
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityContacteBinding

class Contacte : AppCompatActivity() {
    lateinit var binding: ActivityContacteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityContacteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, llistaProblemes())
        binding.tipusProblema.setAdapter(adapter)

        binding.btnEnvia.setOnClickListener {
            val missatge = binding.textFCMissatge.text?.toString()?.trim()
            val tipus = binding.tipusProblema.text?.toString()?.trim()

            if (!missatge.isNullOrEmpty() && !tipus.isNullOrEmpty()) {
                Toast.makeText(this, "S'ha enviat el problema", Toast.LENGTH_LONG).show()
                when (user?.rol) {
                    true -> {
                        startActivity(Intent(this, IniciTaxista::class.java))
                        finish()
                    }
                    false -> {
                        startActivity(Intent(this, IniciUsuari::class.java))
                        finish()
                    }
                    else -> {
                        Toast.makeText(this, "Problemes en el enviament", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Has d'omplir tots els camps", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun llistaProblemes(): List<String>{
        return listOf("Problemes d'accés o compte","Problemes amb l’aplicació","Problemes amb viatges o reserves","Documentació o registre","Pagaments i cobraments", "Valoracions i comentaris","Altres dubtes o consultes")
    }
}