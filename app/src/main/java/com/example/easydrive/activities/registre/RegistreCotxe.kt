package com.example.easydrive.activities.registre

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
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
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.Cotxe
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.UsuariCotxeDTO
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
    private lateinit var textDocAdjuntat: TextView
    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            Log.d("RegixtreCotxe", "URI seleccionada: $uri")
            arxiuTecnic = getFileFromUri(uri)
            cotxe?.fotoFitxaTecnica = arxiuTecnic.toString()
            Log.d("arxiuTecnic launcher", arxiuTecnic.toString())

            if (uri != null) {
                textDocAdjuntat.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistreCotxeBinding.inflate(layoutInflater)
        val obertDesDe = intent.getStringExtra("obert_des_de")
        val esDesDeCotxes = obertDesDe == "cotxes_registrats"

        if (esDesDeCotxes) {
            binding.btnSeguent.text = "Registra cotxe"
        }

        setContentView(binding.root)
        textDocAdjuntat = findViewById(R.id.textDocAdjuntat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        usuari = intent.getSerializableExtra("usuari") as? Usuari
        val dniExtra = intent.getStringExtra("dni")
        rutaPerfil = intent.getStringExtra("ruta")

        carregarComboboxes()


        binding.imagebtnR1.setOnClickListener {
           finish()
        }

        cotxe = Cotxe(null,null,null,null,null,null,null,null,null, null)
        binding.btnSeguent.setOnClickListener {
            val marca = binding.tieMarcaRC.text.toString()
            val model = binding.tieModelRC.text.toString()
            val matricula = binding.tieMatriculaRC.text.toString()
            val any = binding.tieAnyRC.text.toString()
            val tipus = binding.actvTipusRC.text.toString()
            val capacitat = binding.actvCapacitatRC.text.toString()
            val color = binding.tieColorRC.text.toString()

            var isValid = true

            if (marca.isBlank()) {
                binding.til1CardRC.error = "Camp obligatori"
                isValid = false
            } else binding.til1CardRC.error = null

            if (model.isBlank()) {
                binding.til2CardRC.error = "Camp obligatori"
                isValid = false
            } else binding.til2CardRC.error = null

            if (matricula.isBlank()) {
                binding.til3CardRC.error = "Camp obligatori"
                isValid = false
            } else binding.til3CardRC.error = null

            if (any.isBlank()) {
                binding.til4CardRC.error = "Camp obligatori"
                isValid = false
            } else binding.til4CardRC.error = null

            if (tipus.isBlank()) {
                binding.til5CardRC.error = "Camp obligatori"
                isValid = false
            } else binding.til5CardRC.error = null

            if (capacitat.isBlank()) {
                binding.til5CardRCCapacitat.error = "Camp obligatori"
                isValid = false
            } else binding.til5CardRCCapacitat.error = null

            if (color.isBlank()) {
                binding.til6CardRC.error = "Camp obligatori"
                isValid = false
            } else binding.til6CardRC.error = null

            if (!esDesDeCotxes && usuari?.fotoCarnet.isNullOrBlank()) {
                Toast.makeText(this, "Has de pujar una foto del carnet", Toast.LENGTH_LONG).show()
                isValid = false
            }


            if (cotxe?.fotoFitxaTecnica.isNullOrBlank()) {
                Toast.makeText(this, "Has de pujar la documentació tècnica", Toast.LENGTH_LONG).show()
                isValid = false
            }

            if (isValid) {
                cotxe?.matricula = matricula
                cotxe?.marca = marca
                cotxe?.model = model
                cotxe?.any = any
                cotxe?.tipus = tipus
                cotxe?.color = color
                cotxe?.capacitat = capacitat.toInt()

                if (esDesDeCotxes) {
                    val crud = CrudApiEasyDrive()
                    if (crud.insertCotxe(cotxe!!)) {
                        if (crud.updateCotxeFitxaTecnica(cotxe?.matricula.toString(), cotxe?.fotoFitxaTecnica.toString())) {
                            val relacio = UsuariCotxeDTO(
                                dniUsuari  = dniExtra ?: "",
                                matriculaCotxe = cotxe?.matricula ?: ""
                            )
                            Log.d("relacio", relacio.toString())
                            if(crud.insertRelacioCotxeUsuari(relacio)){
                                Toast.makeText(this, "Cotxe registrat correctament", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Hi ha hagut un problema insertant el cotxe...", Toast.LENGTH_SHORT).show()
                        }
                    }
                    setResult(RESULT_OK)
                    finish()
                } else {
                    intentRegistre3()
                }
            }
        }


        if (!esDesDeCotxes) {
            binding.btnPujar1RC.setOnClickListener {
                val galeria = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI
                ).also { imatge ->
                    imatge.type = "image/*"
                    val mimeTypes = arrayOf("image/jpeg", "image/png")
                    imatge.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
                resultLauncher.launch(galeria)
            }

            resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK){
                    val imageUri = result.data?.data
                    usuari?.fotoCarnet = imageUri?.let { comprimirImagen(it) }

                    if (imageUri != null) {
                        binding.imagePreview1.setImageURI(imageUri)
                        binding.imagePreview1.visibility = View.VISIBLE
                    }
                    Log.d("ruta comprimida", usuari?.fotoCarnet.toString())
                }
            }
        } else {
            binding.tvFotoCarnet.visibility = View.GONE
            binding.btnPujar1RC.visibility = View.GONE
            binding.imagePreview1.visibility = View.GONE
        }



        binding.btnPujar2RC.setOnClickListener {
            /*val fitxer = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "text/plain"
            }
            openDocumentLauncher.launch(fitxer)*/
            //openDocumentLauncher.launch(arrayOf("application/octet-stream", "text/plain"))
            openDocumentLauncher.launch(arrayOf("application/pdf"))
        }

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