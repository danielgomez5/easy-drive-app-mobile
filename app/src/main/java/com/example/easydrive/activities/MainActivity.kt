package com.example.easydrive.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.easydrive.R
import com.example.easydrive.activities.interficie_taxista.IniciTaxista
import com.example.easydrive.activities.interficie_usuari.IniciUsuari
import com.example.easydrive.activities.registre.Registre1
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.Usuari
import com.example.easydrive.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val REQUEST_READ_CODE = 1000
    val REQUEST_WRITE_CODE = 1001
    var permis_lectura = false
    var permis_escriptura = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val crud = CrudApiEasyDrive()
        if (comprovaPermisLectura()) {
            permis_lectura = true
        } else {
            if (comprovaPermisDemanatLectura()) {
                permis_lectura = false
            } else {
                demanaPermisLectura()
            }
        }
        if (comprovaPermisEscriptura()) {
            permis_escriptura = true
        } else {
            if (comprovaPermisDemanatEscriptura()) {
                permis_escriptura = false
            } else {
                demanaPermisEscriptura()
            }
        }
        binding.registre.setOnClickListener {
            startActivity(Intent(this, Registre1::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.tieCorreuLogin.text.toString().trim()
            val password = binding.tieContrsenyaLogin.text.toString().trim()

            var isValid = true

            if (email.isEmpty()) {
                binding.til1Card.error = "El correu és obligatori"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.til1Card.error = "Introdueix un correu vàlid"
                isValid = false
            } else {
                binding.til1Card.error = null
            }

            if (password.isEmpty()) {
                binding.til2Card.error = "La contrasenya és obligatòria"
                isValid = false
            } else if (password.length < 6) {
                binding.til2Card.error = "La contrasenya ha de tenir almenys 6 caràcters"
                isValid = false
            } else {
                binding.til2Card.error = null
            }

            if (isValid) {
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )

                lifecycleScope.launch {
                    val usuari = withContext(Dispatchers.IO) {
                        crud.loginUsuari(email, password)
                    }

                    binding.loadingOverlay.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    if (usuari != null) {
                        when (usuari.rol) {
                            false -> {
                                val intent = Intent(this@MainActivity, IniciUsuari::class.java)
                                intent.putExtra("user", usuari)
                                startActivity(intent)
                            }
                            true -> {
                                val intent = Intent(this@MainActivity, IniciTaxista::class.java)
                                intent.putExtra("user", usuari)
                                startActivity(intent)
                            }
                            null -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    "El usuari no està registrat en la aplicació",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "La combinació no es correcta, torna a intentar-ho",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }


    }

    fun comprovaPermisLectura(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun comprovaPermisEscriptura(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun comprovaPermisDemanatLectura(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    fun comprovaPermisDemanatEscriptura(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    fun demanaPermisLectura() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_READ_CODE
        )
    }

    fun demanaPermisEscriptura() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_WRITE_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_READ_CODE -> {
                permis_lectura =
                    grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            }

            REQUEST_WRITE_CODE -> {
                permis_escriptura =
                    grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}