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
import com.example.easydrive.activities.interficie_usuari.IniciUsuari
import com.example.easydrive.api.CrudApiEasyDrive
import com.example.easydrive.dades.Usuari
import com.example.easydrive.databinding.ActivityRegistre3Binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Registre3 : AppCompatActivity() {
    private lateinit var binding : ActivityRegistre3Binding
    private var usuari: Usuari?=null
    private var ruta: String ?=null

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
        usuari = intent.getSerializableExtra("usuari") as? Usuari
        ruta = intent.getStringExtra("ruta")

        Log.d("ruta r3", ruta.toString())
        Log.d("usuari r3", usuari.toString())

        binding.imagebtnR1.setOnClickListener {
            startActivity(Intent(this, Registre2::class.java))
        }

        binding.crearCompte.setOnClickListener {
            if(!binding.tieContrasenyaR3.text.isNullOrBlank() || !binding.tieRepeteixContraR3.text.isNullOrBlank()){
                if (binding.tieContrasenyaR3.text.toString() == binding.tieRepeteixContraR3.text.toString()){
                    usuari?.passwordHash = binding.tieContrasenyaR3.text.toString()
                    val crud = CrudApiEasyDrive()
                    Log.d("crear cuenta", usuari.toString())
                    Log.d("2 if","")
                    if (crud.insertUsuari(usuari!!)){
                        if (crud.updateUserFoto(usuari?.dni!!, ruta!!, "")){
                            Log.d("4 if","s'ha editat")
                            startActivity(Intent(this, IniciUsuari::class.java))
                        }else{
                            Log.d("4 if","no s'ha editat")
                        }
                        Log.d("3 if","s'ha afegit")
                        //startActivity(Intent(this, IniciUsuari::class.java))
                    } else{
                        Toast.makeText(this, "no s'ha afegit", Toast.LENGTH_LONG).show()
                        Log.d("3 if","no s'ha afegit")
                    }
                }
            }
        }
    }
}