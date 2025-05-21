package com.example.easydrive.activities.interficie_taxista

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easydrive.R
import com.example.easydrive.activities.registre.RegistreCotxe
import com.example.easydrive.adaptadors.AdaptadorRVCotxes
import com.example.easydrive.adaptadors.AdapterViatgesPendents
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.Cotxe
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.UsuariCotxeDTO
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityCotxesRegistratsBinding
import com.example.easydrive.databinding.ActivityIniciTaxistaBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CotxesRegistrats : AppCompatActivity() {
    private var selectedCotxe: Cotxe? = null
    private lateinit var registreCotxeLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityCotxesRegistratsBinding
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>
    private var fileCallback: ((File?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                val file = getFileFromUri(it)
                if (file != null) {
                    selectedCotxe?.fotoFitxaTecnica = file.absolutePath

                    Log.d("File selected", "Selected file: ${file.name}")
                } else {
                    Toast.makeText(this, "No s'ha pogut llegir el fitxer", Toast.LENGTH_SHORT).show()
                }
            }
        }

        registreCotxeLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                carregaCotxes()
            }
        }

        enableEdgeToEdge()
        binding = ActivityCotxesRegistratsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        carregaCotxes()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.fabAddCar.setOnClickListener {
            val intent = Intent(this, RegistreCotxe::class.java)
            intent.putExtra("obert_des_de", "cotxes_registrats")
            intent.putExtra("dni", user?.dni)
            registreCotxeLauncher.launch(intent)
        }

        binding.fabAddExistingCar.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_existing_car, null)
            val etMatricula = dialogView.findViewById<EditText>(R.id.et_plate)

            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Afegir") { _, _ ->
                    val matricula = etMatricula.text.toString().trim()
                    if (matricula.isNotEmpty()) {
                        val crud = CrudApiEasyDrive()
                        val relacio = UsuariCotxeDTO(
                            dniUsuari  = user?.dni,
                            matriculaCotxe = matricula
                        )

                        if(crud.insertRelacioCotxeUsuari(relacio)){
                            Toast.makeText(this, "Cotxe registrat correctament", Toast.LENGTH_SHORT).show()
                            carregaCotxes()
                        }else{
                            Toast.makeText(this, "La matrícula no està registrada", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "La matrícula no pot estar buida", Toast.LENGTH_SHORT).show()
                    }

                }
                .setNegativeButton("Cancel·lar", null)
                .show()
        }



    }

    private fun carregaCotxes() {
        val crud = CrudApiEasyDrive()
        var llistaCotxes = mutableListOf<Cotxe>()
        llistaCotxes = crud.getAllCotxesByUsuari(user?.dni!!) as MutableList<Cotxe>

        if (llistaCotxes.isNullOrEmpty()){
            binding.llEmptyState.visibility = View.VISIBLE
        }else{
            binding.llEmptyState.visibility = View.GONE

            binding.rvCotxes.adapter = AdaptadorRVCotxes(
                llista = llistaCotxes,
                onListChanged = { isEmpty -> binding.llEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE },
                context = this,
                documentSelector = { cotxe, callback ->
                    selectedCotxe = cotxe
                    fileCallback = callback
                    openDocumentLauncher.launch(arrayOf("application/pdf"))
                }
            )


            binding.rvCotxes.layoutManager = LinearLayoutManager(this)
        }
    }

    fun getFileFromUri(uri: Uri?): File? {
        if (uri == null) return null

        val fileName = getFileNameFromUri(uri) ?: "default_filename"

        val outputDir = File(getExternalFilesDir(null), "my_files")
        if (!outputDir.exists()) {
            outputDir.mkdir()
        }

        val outputFile = File(outputDir, fileName)

        val inputStream = contentResolver.openInputStream(uri) ?: return null

        return try {
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            outputFile
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
            val proj = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
            cursor = contentResolver.query(uri, proj, null, null, null)
            val columnIndex: Int = cursor?.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME) ?: -1
            cursor?.moveToFirst()

            if (columnIndex != -1) cursor?.getString(columnIndex) else null
        } finally {
            cursor?.close()
        }
    }
}

