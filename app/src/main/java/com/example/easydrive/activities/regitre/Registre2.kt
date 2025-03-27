package com.example.easydrive.activities.regitre

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.activities.MainActivity
import com.example.easydrive.databinding.ActivityRegistre2Binding

class Registre2 : AppCompatActivity() {
    private lateinit var binding: ActivityRegistre2Binding
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
        binding.imagebtnR1.setOnClickListener {
            startActivity(Intent(this, Registre1::class.java))
            finish()
        }

        binding.btnSeguent.setOnClickListener {
            startActivity(Intent(this,Registre2::class.java))
            finish()
        }
    }
}