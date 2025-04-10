package com.example.easydrive.activities.interficie_usuari

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.easydrive.R
import com.example.easydrive.databinding.ActivityIniciUsuariBinding
import com.example.easydrive.fragments.HomeUsuari
import com.example.easydrive.fragments.ViatgesGuardats
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener

class IniciUsuari : AppCompatActivity() , OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityIniciUsuariBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    val REQUESTS_PERMISIONS = 1
    var permisos = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIniciUsuariBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //permisos
        if(comprovarPermisos()){
            permisos = true


        }else{
            demanarPermisos()
        }

        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.main, R.string.obert, R.string.tancat)
        binding.main.addDrawerListener(actionBarDrawerToggle)
        binding.navigator.setNavigationItemSelectedListener(this)

        binding.btnPerfil.setOnClickListener {
            binding.main.openDrawer(GravityCompat.START)
        }

        binding.bnv.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.menuInici -> canviaFragment(HomeUsuari())
                R.id.menuDestinsGuardats -> canviaFragment(ViatgesGuardats())
                else -> canviaFragment(HomeUsuari())
            }
        }

    }


    fun canviaFragment(f : Fragment) : Boolean{
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fcv,f)
        transaction.commit()
        return true
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
    //Permissos necessari de la app
    fun comprovarPermisos() : Boolean{
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }else{
            return false
        }
    }

    fun demanarPermisos(){
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION), REQUESTS_PERMISIONS)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUESTS_PERMISIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permisos = true
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}