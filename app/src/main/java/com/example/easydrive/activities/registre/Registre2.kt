package com.example.easydrive.activities.registre

import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.Zona
import com.example.easydrive.databinding.ActivityRegistre2Binding
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Registre2 : AppCompatActivity() {
    private lateinit var binding: ActivityRegistre2Binding
    private var usuari: Usuari?=null
    private var ruta: String ?=null
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var zonaEscollida: Zona? = null

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
        binding.til5CardR2.visibility = View.GONE
        binding.til6CardR2.visibility = View.GONE
        if (usuari?.rol == true){
            binding.cardHorari.visibility = View.VISIBLE
        } else{
            binding.cardHorari.visibility = View.GONE
        }

        carregaComboBox()

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
                val sdfBD = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = Date(it)
                binding.tieDataNeixR2.setText(sdf.format(date))
                usuari?.dataNaixement = sdfBD.format(date)
            }
        }


        binding.btnSeguent.setOnClickListener {
            if (!binding.tieNomR2.text.isNullOrBlank() && !binding.tieCognomR2.text.isNullOrBlank() && !binding.tieDniR2.text.isNullOrBlank() && !binding.tieDataNeixR2.text.isNullOrBlank()){

                usuari?.nom = binding.tieNomR2.text.toString()
                usuari?.cognom = binding.tieCognomR2.text.toString()
                usuari?.dni = binding.tieDniR2.text.toString()
                usuari?.idZona = zonaEscollida?.id
                when(usuari?.rol){
                    true ->{
                        addTaxista()
                    }
                    false ->{
                        addUsuari()
                    }
                    else -> {
                        addUsuari()
                    }
                }
            } else{
                Toast.makeText(this, "Algun camp està null", Toast.LENGTH_LONG).show()
            }
        }

        /*resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK){
                var imageUri = result.data?.data
                ruta = getRealPathFromUri(imageUri)
                Log.d("ruta launcher", ruta.toString())

            }
        }*/
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK){
                val imageUri = result.data?.data
                //ruta = imageUri?.let { comprimirImagen(it) } // ← Esto es un String
                usuari?.fotoPerfil = imageUri?.let { comprimirImagen(it) }
                Log.d("ruta comprimida", usuari?.fotoPerfil.toString())
            }
        }

        // Luego, en el setOnClickListener, solo lanzas el intent
        binding.btnPujada.setOnClickListener {
            val galeria = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI).also { imatge ->
                imatge.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "image/png")
                imatge.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            resultLauncher.launch(galeria) // Lanza el intent usando el resultLauncher registrado
        }
    }

    private fun carregaComboBox() {
        val crud = CrudApiEasyDrive()
        var zona = crud.getComunitats() ?: listOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, zona)
        binding.actvComunitatR2.setAdapter(adapter)

        var ciutatsZona: List<Zona>? = null
        var nomCiutat: List<String>? = null
        var nomProvincia: List<String>? = null
        binding.actvComunitatR2.setOnItemClickListener { parent, view, position, id ->
            binding.til5CardR2.visibility = View.VISIBLE
            val comunitat = zona[position]

            nomProvincia = crud.getZonaxComunitat(comunitat)

            val adapterProvincia = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nomProvincia!!)
            binding.actvProvinciaR2.setAdapter(adapterProvincia)
        }
        binding.actvProvinciaR2.setOnItemClickListener { parent, view, position, id ->
            binding.til6CardR2.visibility = View.VISIBLE
            val provincia = nomProvincia?.get(position)

            ciutatsZona = crud.getZonaxProvincia(provincia.toString())
            nomCiutat = ciutatsZona?.map { it.ciutat }

            val adapterciutat = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nomCiutat!!)
            binding.actvCiutatR2.setAdapter(adapterciutat)
        }


        binding.actvCiutatR2.setOnItemClickListener { parent, view, position, id ->
            val ciudadSeleccionada = nomCiutat?.get(position)

            if (ciudadSeleccionada != null) {
                zonaEscollida = ciutatsZona?.find { it.ciutat == ciudadSeleccionada }
                Log.d("zonaEscollida", zonaEscollida.toString())
            }
        }
    }

    private fun addTaxista() {
        val intent = Intent(this,RegistreCotxe::class.java)
        intent.putExtra("usuari",usuari)
        intent.putExtra("ruta", ruta)
        Log.d("usuari prova taxi", usuari.toString())
        startActivity(intent)
    }

    private fun addUsuari() {
        val intent = Intent(this,Registre3::class.java)
        intent.putExtra("usuari",usuari)
        intent.putExtra("ruta", ruta)
        Log.d("ruta prova", ruta.toString())
        Log.d("usuari prova", usuari.toString())
        startActivity(intent)
    }

    fun ara(): Long {
        val calendar = Calendar.getInstance()
        return calendar.timeInMillis
    }

    fun getRealPathFromUri(contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = this.getContentResolver().query(contentUri!!, proj, null, null, null)
            val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor!!.moveToFirst()
            cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    fun comprimirImagen(uri: Uri): String? {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val file = File(cacheDir, "compressed_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // Calidad: 50%
        outputStream.flush()
        outputStream.close()
        return file.absolutePath
    }
}