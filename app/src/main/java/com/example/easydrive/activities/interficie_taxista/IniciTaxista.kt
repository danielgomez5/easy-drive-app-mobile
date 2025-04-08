package com.example.easydrive.activities.interficie_taxista

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.databinding.ActivityIniciTaxistaBinding
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener

class IniciTaxista : AppCompatActivity(), OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityIniciTaxistaBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIniciTaxistaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.main, R.string.obert, R.string.tancat)
        binding.main.addDrawerListener(actionBarDrawerToggle)
        binding.navigator.setNavigationItemSelectedListener(this)

        binding.btnPerfil.setOnClickListener {
            binding.main.openDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val menu = binding.navigator.menu
        for (i in 0 until menu.size()) {
            menu.getItem(i).isChecked = false
        }

        item.setChecked(true)
        when(item.itemId){
            R.id.menuPerfil -> {
                Toast.makeText(this,"hola",Toast.LENGTH_LONG).show()
            }

        }

        binding.main.closeDrawer(GravityCompat.START)
        return true
    }
}