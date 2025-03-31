package com.example.easydrive.activities.registre

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.api.CrudApiEasyDrive
import com.example.easydrive.dades.Usuari
import com.example.easydrive.databinding.ActivityRegistre2Binding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Registre2 : AppCompatActivity() {
    private lateinit var binding: ActivityRegistre2Binding
    private var usuari: Usuari?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistre2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usuari = intent.getSerializableExtra("usuari") as? Usuari

        val crud = CrudApiEasyDrive()
        var zona = crud.getZona() ?: listOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, zona)
        binding.actvProvinciaR2.setAdapter(adapter)

        binding.imagebtnR1.setOnClickListener {
            startActivity(Intent(this, Registre1::class.java))
        }

        binding.tieDataNeixR2.setOnClickListener {
            val datePicker: MaterialDatePicker<Long> =
                MaterialDatePicker.Builder.datePicker()
                    .setSelection(ara())
                    .setTitleText("Escull la data").build()
            datePicker.show(supportFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = sdf.format(it)
                binding.tieDataNeixR2.setText(date)
            }
        }

        binding.btnSeguent.setOnClickListener {
            if (!binding.tieNomR2.text.isNullOrBlank() && !binding.tieCognomR2.text.isNullOrBlank() && !binding.tieDniR2.text.isNullOrBlank() && !binding.tieDataNeixR2.text.isNullOrBlank()){

                usuari?.nom = binding.tieNomR2.text.toString()
                usuari?.cognom = binding.tieCognomR2.text.toString()
                usuari?.dni = binding.tieDniR2.text.toString()
                usuari?.data_neix = binding.tieDataNeixR2.text.toString()
                when(usuari?.rol){
                    true ->{
                        addUsuari()
                    }
                    false ->{
                        addTaxista()
                    }
                    else -> {
                        addUsuari()
                    }
                }
            } else{
                Toast.makeText(this, "Algun camp està null", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addTaxista() {
        val intent = Intent(this,Registre3::class.java)
        intent.putExtra("usuari",usuari)
        startActivity(intent)
    }

    private fun addUsuari() {
        val intent = Intent(this,RegistreCotxe::class.java)
        intent.putExtra("usuari",usuari)
        startActivity(intent)
    }

    fun ara(): Long {
        val calendar = Calendar.getInstance()
        return calendar.timeInMillis
    }
}