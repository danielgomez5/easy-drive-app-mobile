package com.example.easydrive.activities.menu

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easydrive.R
import com.example.easydrive.adaptadors.AdaptadorAjuda
import com.example.easydrive.dades.llistaAjudaClient
import com.example.easydrive.dades.llistaAjudaTaxista
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityAjudaBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class Ajuda : AppCompatActivity() {
    private lateinit var binding: ActivityAjudaBinding
    private lateinit var layout_bottom_sheet: ConstraintLayout
    private lateinit var bottom_behavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAjudaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        layout_bottom_sheet = findViewById(R.id.layout_bottom_sheet_Ajuda)
        bottom_behavior = BottomSheetBehavior.from(layout_bottom_sheet)

        val btnCerrar: ImageButton = layout_bottom_sheet.findViewById(R.id.btnCerrar)
        btnCerrar.setOnClickListener {
            bottom_behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }


        if (user?.rol!!){
            binding.rcvAjuda.adapter = AdaptadorAjuda(llistaAjudaTaxista,layout_bottom_sheet,
                bottom_behavior)
            binding.rcvAjuda.layoutManager = LinearLayoutManager(this)
        }else{
            binding.rcvAjuda.adapter = AdaptadorAjuda(llistaAjudaClient,layout_bottom_sheet,
                bottom_behavior)
            binding.rcvAjuda.layoutManager = LinearLayoutManager(this)
        }

        binding.imagebtnR1.setOnClickListener {
            finish()
        }

        binding.btnContacte.setOnClickListener {
            startActivity(Intent(this, Contacte::class.java))
            finish()
        }
    }
}