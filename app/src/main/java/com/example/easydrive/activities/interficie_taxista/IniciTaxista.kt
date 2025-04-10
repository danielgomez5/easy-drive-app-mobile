package com.example.easydrive.activities.interficie_taxista

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.databinding.ActivityIniciTaxistaBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener

class IniciTaxista : AppCompatActivity(), OnNavigationItemSelectedListener , OnMapReadyCallback, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener{
    private lateinit var binding: ActivityIniciTaxistaBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    val REQUESTS_PERMISIONS = 1
    var permisos = false
    var map: GoogleMap? = null

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
    }
    override fun onMapReady(googleMap: GoogleMap){
        map = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        map?.isMyLocationEnabled = true
        map?.setOnMyLocationClickListener(this)
        map?.setOnMyLocationButtonClickListener(this)
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Ubicación actual: ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Botón de ubicación pulsado", Toast.LENGTH_SHORT).show()
        return false
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