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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
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
        carregaDisponibilitat()

        binding.imagebtnR1.setOnClickListener {
            startActivity(Intent(this, Registre1::class.java))
        }

        binding.tieDataNeixR2.setOnClickListener {
            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona la teva data de naixement")
                .setCalendarConstraints(constraints)
                .build()

            datePicker.show(supportFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val selectedDate = Date(selection)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val sdfBD = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                // Mostrar la fecha en el campo, aunque sea inválida
                binding.tieDataNeixR2.setText(sdf.format(selectedDate))

                // Guardar la fecha en el objeto (a validar después)
                usuari?.dataNaixement = sdfBD.format(selectedDate)

                // Limpiar posibles errores antiguos
                binding.tieDataNeixR2.error = null
            }
        }


        binding.btnSeguent.setOnClickListener {
            val sdfBD = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = usuari?.dataNaixement

            if (dateStr.isNullOrEmpty()) {
                binding.tieDataNeixR2.error = "Has d’introduir una data"
                return@setOnClickListener
            }

            val birthDate = sdfBD.parse(dateStr)
            val today = Calendar.getInstance()
            val birthCal = Calendar.getInstance().apply { time = birthDate!! }

            var age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                age--
            }

            if (age < 18) {
                Toast.makeText(this, "Has de ser major d’edat", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            binding.tieDataNeixR2.error = null

            var valid = true

            if (binding.tieNomR2.text.isNullOrBlank()) {
                binding.tieNomR2.error = "Aquest camp és obligatori"
                valid = false
            } else binding.tieNomR2.error = null

            if (binding.tieCognomR2.text.isNullOrBlank()) {
                binding.tieCognomR2.error = "Aquest camp és obligatori"
                valid = false
            } else binding.tieCognomR2.error = null

            if (binding.tieDniR2.text.isNullOrBlank()) {
                binding.tieDniR2.error = "Aquest camp és obligatori"
                valid = false
            } else binding.tieDniR2.error = null

            if (binding.tieDataNeixR2.text.isNullOrBlank()) {
                binding.tieDataNeixR2.error = "Aquest camp és obligatori"
                valid = false
            }

            if (zonaEscollida == null) {
                Toast.makeText(this, "Has d’escollir una zona", Toast.LENGTH_LONG).show()
                valid = false
            }

            if (valid) {
                usuari?.nom = binding.tieNomR2.text.toString()
                usuari?.cognom = binding.tieCognomR2.text.toString()
                usuari?.dni = binding.tieDniR2.text.toString()
                usuari?.idZona = zonaEscollida?.id

                when (usuari?.rol) {
                    true -> {
                        usuari?.horari = binding.actvDIniciR2.text.toString()+"-"+binding.actvDFinalR2.text.toString()+";"+binding.actvHIniciR2.text.toString()+"-"+binding.actvHFinalR2.text.toString()
                        usuari?.diponibiliat = false
                        addTaxista()
                    }
                    false -> addUsuari()
                    else -> addUsuari()
                }
            } else {
                Toast.makeText(this, "Omple els camps correctament", Toast.LENGTH_LONG).show()
            }
        }

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri = result.data!!.data

                binding.imagePreview.setImageURI(imageUri)
                binding.imagePreview.visibility = View.VISIBLE

                binding.textInfoPujada.text = "Foto pujada!"

                usuari?.fotoPerfil = imageUri?.let { comprimirImagen(it) }

                Log.d("ruta comprimida", usuari?.fotoPerfil.toString())
            } else {
                binding.textInfoPujada.text = "Cap imatge seleccionada"
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

    fun comprimirImagen(uri: Uri): String? {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val file = File(cacheDir, "compressed_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream) // Calidad: 30%
        outputStream.flush()
        outputStream.close()
        return file.absolutePath
    }
    private fun carregaDisponibilitat() {
        val dies = listOf("Dilluns", "Dimarts", "Dimecres", "Dijous", "Divendres", "Dissabte", "Diumenge")
        val hores = mutableListOf<String>()

        for (hora in 0..23) {
            hores.add(String.format("%02d:00", hora))
            hores.add(String.format("%02d:30", hora))
        }

        val adapterDies = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dies)
        val adapterHores = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, hores)

        // Dia i hora d'inici

        binding.actvDIniciR2.setAdapter(adapterDies)
        binding.actvHIniciR2.setAdapter(adapterHores)

        // Dia i hora de fi
        binding.actvDFinalR2.setAdapter(adapterDies)
        binding.actvHFinalR2.setAdapter(adapterHores)
    }

}