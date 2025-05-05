package com.example.easydrive.activities.menu

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.ChangePasswordRequest
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityPerfilBinding

class Perfil : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilBinding
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

        binding.tieNom.isEnabled = false
        binding.tieCognom.isEnabled = false
        binding.tieDataNaix.isEnabled = false
        binding.tieEmail.isEnabled = false
        binding.tieTelefon.isEnabled = false

        binding.tvBenvinguda.setText("El teu perfil, ${user?.nom}!")

        binding.tieNom.setText(user?.nom)
        binding.tieCognom.setText(user?.cognom)
        binding.tieDataNaix.setText(user?.dataNaixement)
        binding.tieEmail.setText(user?.email)
        binding.tieTelefon.setText(user?.telefon)


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


        binding.btnDesaCanvis.setOnClickListener {
            val nouUsuari = user?.copy(
                nom = binding.tieNom.text.toString(),
                cognom = binding.tieCognom.text.toString(),
                dataNaixement = binding.tieDataNaix.text.toString(),
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
                    Toast.makeText(this, "Hi ha hagut un problema modificant les dades", Toast.LENGTH_LONG).show()
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
                binding.tiePassword1.error = "La contrasenya ha de tenir almenys 6 caràcters"
                valid = false
            }

            if (newP1 != newP2) {
                binding.tiePassword2.error = "Les contrasenyes no coincideixen"
                valid = false
            }

            if (!valid) return@setOnClickListener

            val crud = CrudApiEasyDrive()
            val request = ChangePasswordRequest(
                NovaContrasenya = newP1
            )

            if (crud.changePassword(user?.dni, request)) {
                Toast.makeText(this, "Contrasenya actualitzada correctament", Toast.LENGTH_LONG).show()
            } else{
                Toast.makeText(this, "Hi ha hagut un error canvian't la contrasenya...", Toast.LENGTH_LONG).show()

            }
        }
    }
}