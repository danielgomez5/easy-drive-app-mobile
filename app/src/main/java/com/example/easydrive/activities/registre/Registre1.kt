package com.example.easydrive.activities.registre

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
            startActivity(Intent(this,MainActivity::class.java))
        }

        var usuari = Usuari(null, null,null,null,null,null,null,null,null,null,null,null,null)
        binding.btnSeguent.setOnClickListener {
            if (identificador != -1) {
                if (!binding.tieNumMobilR1.text.isNullOrBlank() || !binding.tieCorreuR1.text.isNullOrBlank() || !binding.tieRepeteixCorreuR1.text.isNullOrBlank()){
                    if (binding.tieCorreuR1.text.toString().equals(binding.tieRepeteixCorreuR1.text.toString())){
                        if (identificador == 1){
                            usuari.rol = true
                        } else{
                            usuari.rol = false
                        }

                        usuari.telefon = binding.tieNumMobilR1.text?.toString()
                        usuari.email = binding.tieCorreuR1.text?.toString()
                        val intent = Intent(this,Registre2::class.java)
                        intent.putExtra("usuari",usuari)
                        Log.d("prova usuari", usuari.toString())
                        startActivity(intent)
                    }else{
                        Toast.makeText(this, "Correu no consideix",Toast.LENGTH_LONG).show()
                    }
                } else{
                    Toast.makeText(this, "error",Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this, "Error escull bla bla 2 opcions", Toast.LENGTH_LONG).show()
            }


        }

        binding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked){
                when(checkedId){
                    R.id.btnUsuari -> {
                        identificador = 0
                    }
                    R.id.btnTaxista -> {
                        identificador = 1
                    }
                }
            }
        }

    }
}