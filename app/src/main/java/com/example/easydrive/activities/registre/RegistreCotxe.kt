package com.example.easydrive.activities.registre

import android.content.Intent
import android.database.Cursor
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easydrive.R
import com.example.easydrive.activities.MainActivity
import com.example.easydrive.dades.Cotxe
import com.example.easydrive.dades.Usuari
import com.example.easydrive.databinding.ActivityRegistreCotxeBinding
import java.io.File
import java.io.IOException

class RegistreCotxe : AppCompatActivity() {
    private lateinit var binding: ActivityRegistreCotxeBinding
    private var usuari: Usuari? = null
    private var rutaPerfil: String? = null
    private var fotoCarnet: String? = null
    private var arxiuTecnic: File? = null
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    //private lateinit var openDocumentLauncher: ActivityResultLauncher<Intent>

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            Log.d("RegixtreCotxe", "URI seleccionada: $uri")
            arxiuTecnic= getFileFromUri(uri)
            Log.d("arxiuTecnic launcher", arxiuTecnic.toString())
            /*uri?.let {
                arxiuTecnic= getRealPathFromUri(uri)
                Log.d("arxiuTecnic launcher", arxiuTecnic.toString())
            }*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistreCotxeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        usuari = intent.getSerializableExtra("usuari") as? Usuari
        rutaPerfil = intent.getStringExtra("ruta")
        var cotxe: Cotxe? = null

        carregarComboboxes()

        binding.imagebtnR1.setOnClickListener {
            startActivity(Intent(this, Registre2::class.java))
        }


        binding.btnSeguent.setOnClickListener {

            if (!binding.tieMarcaRC.text.isNullOrBlank() && !binding.tieModelRC.text.isNullOrBlank() && !binding.tieMatriculaRC.text.isNullOrBlank()
                && binding.tieAnyRC.text.isNullOrBlank() && !binding.actvTipusRC.text.isNullOrBlank() && binding.tieColorRC.text.isNullOrBlank() && !binding.actvCapacitatRC.text.isNullOrBlank()
            ) {
                cotxe?.matricula = binding.tieMatriculaRC.text.toString()
                cotxe?.marca = binding.tieMarcaRC.text.toString()
                cotxe?.model = binding.tieModelRC.text.toString()
                cotxe?.any = binding.tieAnyRC.text.toString()
                cotxe?.tipus = binding.actvTipusRC.text.toString()
                cotxe?.color = binding.tieColorRC.text.toString()
                cotxe?.capacitat = binding.actvCapacitatRC.text.toString().toInt()
                intentRegistre3()
            } else {
                Toast.makeText(this, "Algun camp està vuit", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnPujar1RC.setOnClickListener {
            val galeria = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            ).also { imatge ->
                imatge.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "image/png")
                imatge.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            resultLauncher.launch(galeria) // Lanza el intent usando el resultLauncher registrado
        }
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    var imageUri = result.data?.data

                    fotoCarnet = getRealPathFromUri(imageUri)
                    Log.d("fotoCarnet launcher", fotoCarnet.toString())

                }
            }

        binding.btnPujar2RC.setOnClickListener {
            /*val fitxer = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "text/plain"
            }
            openDocumentLauncher.launch(fitxer)*/
            openDocumentLauncher.launch(arrayOf("application/octet-stream", "text/plain"))
        }

        /*resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val fileUri = result.data?.data
                    arxiuTecnic = getRealPathFromUri(fileUri)
                    Log.d("arxiuTecnic launcher", arxiuTecnic.toString())
                }
            }*/


    }

    private fun intentRegistre3() {
        val intent = Intent(this,Registre3::class.java)
        intent.putExtra("usuari",usuari)
        intent.putExtra("ruta", rutaPerfil)
        Log.d("ruta prova", rutaPerfil.toString())
        Log.d("usuari prova", usuari.toString())
        startActivity(intent)
    }

    private fun carregarComboboxes() {
        var adapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, carragarLlistaTipus())
        binding.actvTipusRC.setAdapter(adapter)
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            carregarLlistaCapacitat()
        )
        binding.actvCapacitatRC.setAdapter(adapter)
    }

    fun carragarLlistaTipus(): List<String> {
        return listOf("Petit", "Mitjà", "Gran")
    }

    fun carregarLlistaCapacitat(): List<String> {
        return listOf("2", "4", "5", "7", "8", "12")
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

    fun getFileFromUri(uri: Uri?): File? {
        if (uri == null) return null

        val inputStream = this.getContentResolver().openInputStream(uri) ?: return null
        val file = File(this.cacheDir, "temp_file")

        return try {
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            inputStream.close()
        }
    }


}