package com.example.easydrive.activities.registre

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.activities.interficie_taxista.IniciTaxista
import com.example.easydrive.activities.interficie_usuari.IniciUsuari
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.Cotxe
import com.example.easydrive.dades.Usuari
import com.example.easydrive.databinding.ActivityRegistre3Binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class Registre3 : AppCompatActivity() {
    private lateinit var binding: ActivityRegistre3Binding
    private var usuari: Usuari? = null
    private var ruta: String? = null
    private var fotoCarnet: String? = null
    private var arxiuTecnic: String? = null
    private var cotxe: Cotxe? = null

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

        cotxe = intent.getSerializableExtra("cotxe") as? Cotxe
        fotoCarnet = intent.getStringExtra("fotoCarnet")
        arxiuTecnic = intent.getStringExtra("arxiuTecnic")

        Log.d("ruta r3", ruta.toString())
        Log.d("usuari r3", usuari.toString())
        Log.d("cotxe r3", cotxe.toString())

        binding.imagebtnR1.setOnClickListener {
            if (usuari?.rol == true) {
                startActivity(Intent(this, RegistreCotxe::class.java))
            } else {
                startActivity(Intent(this, Registre2::class.java))
            }

        }

        binding.crearCompte.setOnClickListener {
            if (!binding.tieContrasenyaR3.text.isNullOrBlank() || !binding.tieRepeteixContraR3.text.isNullOrBlank()) {
                if (binding.tieContrasenyaR3.text.toString() == binding.tieRepeteixContraR3.text.toString()) {
                    usuari?.passwordHash = binding.tieContrasenyaR3.text.toString()
                    val crud = CrudApiEasyDrive()
                    Log.d("crear cuenta", usuari.toString())
                    Log.d("2 if", "")

                    if (usuari?.rol == true) {
                        addTaxista(crud)
                    } else {
                        addUsuari(crud)
                    }
                }
            }
        }
    }

    private fun addUsuari(crud: CrudApiEasyDrive) {
        if (crud.insertUsuari(usuari!!)) {
            if (usuari?.fotoPerfil!=null){
                if (crud.updateUserFoto(usuari?.dni!!, usuari?.fotoPerfil)){
                    Log.d("update foto","s'ha editat")

                }else{
                    Log.d("update foto","no s'ha editat")
                }
            }
            Log.d("3 if", "s'ha afegit")
            val intent = Intent(this, IniciUsuari::class.java)
            intent.putExtra("user", usuari)
            startActivity(intent)
        } else {
            Toast.makeText(this, "no s'ha afegit", Toast.LENGTH_LONG).show()
            Log.d("3 if", "no s'ha afegit")
        }
    }

    private fun addTaxista(crud: CrudApiEasyDrive) {
        var taxistaAfegit = false
        var cotxeAfegit = false
        if (crud.insertUsuari(usuari!!)) {
            if (crud.updateUserFotoPerfilCarnet(usuari?.dni!!, usuari?.fotoPerfil, usuari?.fotoCarnet)){
                Log.d("fotos afegides TAxistas", "s'ha editat")
                if (crud.updateZonaCoberta(usuari?.idZona.toString())) {
                    taxistaAfegit = true
                    Log.d("if zona cuberta", "s'ha editat")
                }else{
                    Log.d("if zona cuberta", "NO s'ha editat")
                }
            }else{
                Log.d("fotos afegides TAxistas", "NO s'ha editat")
            }
            Log.d("insert taxista", "s'ha afegit")
        } else {
            Toast.makeText(this, "no s'ha afegit", Toast.LENGTH_LONG).show()
            Log.d("insert taxista", "no s'ha afegit")
        }

        if (crud.insertCotxe(cotxe!!)) {
            Log.d("insert cotxe", "s'ha afegit")
            if (crud.updateCotxeFitxaTecnica(cotxe?.matricula.toString(), cotxe?.fotoFitxaTecnica.toString())) {
                Log.d("if insert foto cotxe", "s'ha editat")
                cotxeAfegit = true
            } else {
                Log.d("if insert foto cotxe", "NO s'ha editat")
            }
        } else {
            Toast.makeText(this, "no s'ha afegit", Toast.LENGTH_LONG).show()
            Log.d("insert cotxe", "no s'ha afegit")
        }

        if (taxistaAfegit && cotxeAfegit) {
            val intent = Intent(this, IniciTaxista::class.java)
            intent.putExtra("user", usuari)
            startActivity(intent)
        }
    }
}