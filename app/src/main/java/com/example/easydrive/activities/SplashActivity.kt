package com.example.easydrive.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.easydrive.activities.interficie_taxista.IniciTaxista
import com.example.easydrive.activities.interficie_usuari.IniciUsuari
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val dniGuardat = sharedPref.getString("dni", null)

        if (dniGuardat != null) {
            val crud = CrudApiEasyDrive()
            lifecycleScope.launch {
                val usuari = withContext(Dispatchers.IO) {
                    try {
                        crud.getUsuariById(dniGuardat)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (usuari != null) {
                    val intent = if (usuari.rol == true) {
                        Intent(this@SplashActivity, IniciTaxista::class.java)
                    } else {
                        Intent(this@SplashActivity, IniciUsuari::class.java)
                    }
                    intent.putExtra("user", usuari)
                    startActivity(intent)
                    finish()
                } else {
                    irALogin()
                }
            }
        } else {
            irALogin()
        }
    }

    private fun irALogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
