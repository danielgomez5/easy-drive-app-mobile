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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easydrive.R
import com.example.easydrive.activities.MainActivity
import com.example.easydrive.dades.Cotxe
import com.example.easydrive.dades.Usuari
import com.example.easydrive.databinding.ActivityRegistreCotxeBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RegistreCotxe : AppCompatActivity() {
    private lateinit var binding: ActivityRegistreCotxeBinding
    private var usuari: Usuari? = null
    private var rutaPerfil: String? = null
    private var fotoCarnet: String? = null
    private var arxiuTecnic: File? = null
    //private var arxiuTecnic: String? = null
    private var cotxe: Cotxe? = null
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    //private lateinit var openDocumentLauncher: ActivityResultLauncher<Intent>

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            Log.d("RegixtreCotxe", "URI seleccionada: $uri")

                //arxiuTecnic = getRealPathFromUri(uri)
            arxiuTecnic = getFileFromUri(uri)
                cotxe?.fotoFitxaTecnica = arxiuTecnic.toString()
                Log.d("arxiuTecnic launcher", arxiuTecnic.toString())
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

        carregarComboboxes()

        binding.imagebtnR1.setOnClickListener {
            startActivity(Intent(this, Registre2::class.java))
        }
        cotxe = Cotxe(null,null,null,null,null,null,null,null,null)
        binding.btnSeguent.setOnClickListener {
            if (!binding.tieMarcaRC.text.isNullOrBlank() && !binding.tieModelRC.text.isNullOrBlank() && !binding.tieMatriculaRC.text.isNullOrBlank()
                && !binding.tieAnyRC.text.isNullOrBlank() && !binding.actvTipusRC.text.isNullOrBlank() && !binding.tieColorRC.text.isNullOrBlank() && !binding.actvCapacitatRC.text.isNullOrBlank()
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
                if (result.resultCode == RESULT_OK){
                    val imageUri = result.data?.data
                    //ruta = imageUri?.let { comprimirImagen(it) } // ← Esto es un String
                    usuari?.fotoCarnet = imageUri?.let { comprimirImagen(it) }
                    Log.d("ruta comprimida", usuari?.fotoCarnet.toString())
                }
            }

        binding.btnPujar2RC.setOnClickListener {
            /*val fitxer = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "text/plain"
            }
            openDocumentLauncher.launch(fitxer)*/
            //openDocumentLauncher.launch(arrayOf("application/octet-stream", "text/plain"))
            openDocumentLauncher.launch(arrayOf("application/pdf"))
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
        intent.putExtra("cotxe",cotxe)
        intent.putExtra("fotoCarnet", fotoCarnet)
        intent.putExtra("arxiuTecnic", arxiuTecnic.toString())

        Log.d("ruta prova", rutaPerfil.toString())
        Log.d("usuari prova", usuari.toString())
        Log.d("cotxe prova", cotxe.toString())
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
            // Consulta para obtener la ruta del archivo
            val proj = arrayOf(MediaStore.Files.FileColumns.DATA)
            cursor = this.contentResolver.query(contentUri!!, proj, null, null, null)
            val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            cursor.moveToFirst()

            // Comprobamos si la ruta existe
            val filePath = cursor.getString(column_index)
            Log.d("getRealPathFromUri", "Ruta del archivo: $filePath")
            filePath
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

    /*fun getFileFromUri(uri: Uri?): File? {
        if (uri == null) return null

        val inputStream = this.getContentResolver().openInputStream(uri) ?: return null
        val file = File(this.cacheDir)

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
    }*/
    fun getFileFromUri(uri: Uri?): File? {
        if (uri == null) return null

        // Obtén el nombre del archivo
        val fileName = getFileNameFromUri(uri) ?: "default_filename"

        // Crea un archivo en un directorio específico (por ejemplo, el directorio de documentos)
        val outputDir = File(getExternalFilesDir(null), "my_files") // Puedes cambiar esta ubicación según lo necesites
        if (!outputDir.exists()) {
            outputDir.mkdir() // Crea el directorio si no existe
        }

        val outputFile = File(outputDir, fileName)

        val inputStream = contentResolver.openInputStream(uri) ?: return null

        return try {
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            outputFile // Retorna el archivo
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            inputStream.close()
        }
    }

    fun getFileNameFromUri(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            // Consulta para obtener el nombre del archivo
            val proj = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
            cursor = contentResolver.query(uri, proj, null, null, null)
            val columnIndex: Int = cursor?.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME) ?: -1
            cursor?.moveToFirst()

            // Si se encuentra el nombre, lo devuelve
            if (columnIndex != -1) cursor?.getString(columnIndex) else null
        } finally {
            cursor?.close()
        }
    }



}