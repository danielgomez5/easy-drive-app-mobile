package com.example.easydrive.activities.menu

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.easydrive.R
import com.example.easydrive.activities.MainActivity
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.ChangePasswordRequest
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityPerfilBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Perfil : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        disableEditMode()
        enableEditMode()
        binding.tvBenvinguda.setText("El teu perfil, ${user?.nom}!")

        binding.tvDni.setText("DNI: ${user?.dni}")
        binding.tieNom.setText(user?.nom)
        binding.tieCognom.setText(user?.cognom)
        binding.tieDataNaix.setText(user?.dataNaixement)
        binding.tieEmail.setText(user?.email)
        binding.tieTelefon.setText(user?.telefon)

        loadImgPerfil()


        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri = result.data!!.data

                user?.fotoPerfil = imageUri?.let { comprimirImagen(it) }


                val crud = CrudApiEasyDrive()
                Log.d("ruta comprimida", user?.fotoPerfil.toString())

                if (crud.updateUserFoto(user?.dni!!, user?.fotoPerfil)) {
                    Log.d("update foto", "s'ha editat")
                    loadImgPerfil()

                } else {
                    Log.d("update foto", "no s'ha editat")
                }
            }
        }

        binding.imgPerfil.setOnClickListener {
            val galeria = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI).also { imatge ->
                imatge.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "image/png")
                imatge.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            resultLauncher.launch(galeria)
        }

        binding.btnDesaCanvis.setOnClickListener {
            val sdfBD = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = user?.dataNaixement

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

            binding.tieDataNaix.error = null

            val nouUsuari = user?.copy(
                nom = binding.tieNom.text.toString(),
                cognom = binding.tieCognom.text.toString(),
                dataNaixement = user?.dataNaixement,
                email = binding.tieEmail.text.toString(),
                telefon = binding.tieTelefon.text.toString()
            )

            if (nouUsuari != null) {
                val crud = CrudApiEasyDrive()

                if (crud.updateUsuari(nouUsuari.dni, nouUsuari)) {
                    user = nouUsuari

                    binding.tieNom.setText(nouUsuari.nom)
                    binding.tieCognom.setText(nouUsuari.cognom)
                    binding.tieDataNaix.setText(nouUsuari.dataNaixement)
                    binding.tieEmail.setText(nouUsuari.email)
                    binding.tieTelefon.setText(nouUsuari.telefon)

                    Toast.makeText(this, "Dades actualitzades correctament", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        this,
                        "Hi ha hagut un problema modificant les dades",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        binding.btnCanviarContrasenya.setOnClickListener {
            val newP1 = binding.tiePassword1.text.toString()
            val newP2 = binding.tiePassword2.text.toString()

            binding.tiePassword1.error = null
            binding.tiePassword2.error = null

            var valid = true

            if (newP1.length < 6) {
                binding.tilPassword1.error =
                    "La contrasenya ha de tenir almenys 6 caràcters"
                valid = false
            }

            if (newP1 != newP2) {
                binding.tilPassword2.error = "Les contrasenyes no coincideixen"
                valid = false
            }

            if (!valid) return@setOnClickListener

            val crud = CrudApiEasyDrive()
            val request = ChangePasswordRequest(
                NovaContrasenya = newP1
            )

            if (crud.changePassword(user?.dni, request)) {
                Toast.makeText(
                    this,
                    "Contrasenya actualitzada correctament",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Hi ha hagut un error canvian't la contrasenya...",
                    Toast.LENGTH_LONG
                ).show()

            }
        }

        binding.btnEliminar.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(this)
                .setIcon(this.getDrawable(R.drawable.baseline_auto_delete_24))
                .setMessage("Aquesta opció serà irreversible i totes les teves dades es perdran.")
                .setTitle("Estàs segur d'eliminar el compte?")
                .setPositiveButton("Acceptar") { dialog, wich ->
                    val crud = CrudApiEasyDrive()

                    if (crud.delUser(user?.dni!!)) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        user = null
                        Toast.makeText(
                            this,
                            "Compte el·liminat permanentment",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        Toast.makeText(
                            this,
                            "Hi ha hagut un problema el·liminant el teu compte",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .setNegativeButton("Cancel·lar") { dialog, wich -> }
            dialog.show()
        }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            user = null
            Toast.makeText(this, "Sessió tancada", Toast.LENGTH_LONG).show()
        }

        binding.tilDataNaix.setEndIconOnClickListener {
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

                binding.tieDataNaix.setText(sdf.format(selectedDate))
                user?.dataNaixement = sdfBD.format(selectedDate)

                binding.tieDataNaix.error = null
            }
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        user?.dataNaixement?.let { dateString ->
            val sdfBD = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = sdfBD.parse(dateString)
            if (parsedDate != null) {
                binding.tieDataNaix.setText(sdf.format(parsedDate))
            }
        }


        binding.imagebtnR1.setOnClickListener {
            finish()
        }
    }

    fun enableEditMode(){
        binding.tilNom.setEndIconOnClickListener {
            binding.tieNom.isEnabled = true
            binding.tieNom.requestFocus()
        }
        binding.tilCognom.setEndIconOnClickListener {
            binding.tieCognom.isEnabled = true
            binding.tieCognom.requestFocus()
        }
        binding.tilDataNaix.setEndIconOnClickListener {
            binding.tieDataNaix.isEnabled = true
            binding.tieDataNaix.requestFocus()
        }
        binding.tilMail.setEndIconOnClickListener {
            binding.tieEmail.isEnabled = true
            binding.tieEmail.requestFocus()
        }
        binding.tilTelefon.setEndIconOnClickListener {
            binding.tieTelefon.isEnabled = true
            binding.tieTelefon.requestFocus()
        }
    }

    fun disableEditMode(){
        binding.tieNom.isEnabled = false
        binding.tieCognom.isEnabled = false
        binding.tieDataNaix.isEnabled = false
        binding.tieEmail.isEnabled = false
        binding.tieTelefon.isEnabled = false
    }

    fun comprimirImagen(uri: Uri): String? {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val file = File(cacheDir, "compressed_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
        outputStream.flush()
        outputStream.close()
        return file.absolutePath
    }

    fun loadImgPerfil(){
        if (user?.fotoPerfil!=null){
            Glide.with(this)
                .load("http://172.16.24.115:7126/Photos/${File(user?.fotoPerfil).name}")
                .error(R.drawable.add_pic)
                .into(binding.imgPerfil)

            user?.fotoPerfil = File(user?.fotoPerfil).name
        }else{
            binding.imgPerfil.setImageResource(R.drawable.add_pic)
        }
    }
}