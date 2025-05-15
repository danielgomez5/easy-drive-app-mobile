package com.example.easydrive.activities.registre

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.activities.MainActivity
import com.example.easydrive.dades.Usuari
import com.example.easydrive.databinding.ActivityRegistre1Binding

class Registre1 : AppCompatActivity() {
    private lateinit var binding: ActivityRegistre1Binding
    private var identificador: Int = -1
    var usuari: Usuari?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistre1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.imagebtnR1.setOnClickListener {
            finish()
        }

        usuari = Usuari(null, null,null,null,null,null,null,null,null,null,null,null,null,null)
        binding.btnSeguent.setOnClickListener {
            var isValid = true

            binding.tieNumMobilR1.error = null
            binding.tieCorreuR1.error = null
            binding.tieRepeteixCorreuR1.error = null

            // Validar que se ha seleccionado una opción del toggle
            if (binding.toggleButton.checkedButtonId == View.NO_ID) {
                Toast.makeText(this, "Selecciona una opció", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val telefon = binding.tieNumMobilR1.text?.toString()?.trim()
            if (telefon.isNullOrEmpty()) {
                binding.tieNumMobilR1.error = "Introdueix un número"
                isValid = false
            } else if (telefon.length != 9) {
                binding.tieNumMobilR1.error = "Ha de tenir 9 dígits"
                isValid = false
            }

            val correu = binding.tieCorreuR1.text?.toString()?.trim()
            val repeteixCorreu = binding.tieRepeteixCorreuR1.text?.toString()?.trim()

            if (correu.isNullOrEmpty()) {
                binding.tieCorreuR1.error = "Introdueix un correu"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(correu).matches()) {
                binding.tieCorreuR1.error = "Correu electrònic no vàlid"
                isValid = false
            }

            if (repeteixCorreu.isNullOrEmpty()) {
                binding.tieRepeteixCorreuR1.error = "Repeteix el correu"
                isValid = false
            } else if (correu != repeteixCorreu) {
                binding.tieRepeteixCorreuR1.error = "El correu no coincideix"
                isValid = false
            }


            if (isValid) {
                usuari?.telefon = telefon
                usuari?.email = correu

                val intent = Intent(this, Registre2::class.java)
                intent.putExtra("usuari", usuari)
                startActivity(intent)
            }
        }

        binding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked){
                when(checkedId){
                    R.id.btnUsuari -> {
                        usuari?.rol = false
                    }
                    R.id.btnTaxista -> {
                        usuari?.rol = true
                    }
                }
            }
        }

    }
}