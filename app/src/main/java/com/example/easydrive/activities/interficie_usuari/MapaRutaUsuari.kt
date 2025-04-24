package com.example.easydrive.activities.interficie_usuari

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.dades.rutaEscollida
import com.example.easydrive.databinding.ActivityMapaRutaUsuariBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MapaRutaUsuari : AppCompatActivity() {
    private lateinit var binding: ActivityMapaRutaUsuariBinding
    private lateinit var layout_bottom_sheet : ConstraintLayout
    private lateinit var bottom_behavior : BottomSheetBehavior<ConstraintLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapaRutaUsuariBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.tieDestiMapausuari.setText(rutaEscollida?.address_line1+", "+rutaEscollida?.city)

        layout_bottom_sheet = findViewById(R.id.layout_bottom_sheet)
        bottom_behavior = BottomSheetBehavior.from(layout_bottom_sheet)

        binding.btnConfirmar.setOnClickListener {
            bottom_behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}