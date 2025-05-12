package com.example.easydrive.activities.interficie_taxista

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easydrive.R
import com.example.easydrive.adaptadors.AdaptadorRVCotxes
import com.example.easydrive.adaptadors.AdapterViatgesPendents
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.Cotxe
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityCotxesRegistratsBinding
import com.example.easydrive.databinding.ActivityIniciTaxistaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CotxesRegistrats : AppCompatActivity() {
    private lateinit var binding: ActivityCotxesRegistratsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCotxesRegistratsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        carregaCotxes()

    }

    private fun carregaCotxes() {
        val crud = CrudApiEasyDrive()
        var llistaCotxes = mutableListOf<Cotxe>()
        llistaCotxes = crud.getAllCotxesByUsuari(user?.dni!!) as MutableList<Cotxe>

        if (llistaCotxes.isNullOrEmpty()){
            binding.llEmptyState.visibility = View.VISIBLE
        }else{
            binding.llEmptyState.visibility = View.GONE

            binding.rvCotxes.adapter = AdaptadorRVCotxes(llistaCotxes) { isEmpty ->
                binding.llEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            }
            binding.rvCotxes.layoutManager = LinearLayoutManager(this)
        }
    }
}