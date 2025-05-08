package com.example.easydrive.activities.interficie_taxista

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.easydrive.R
import com.example.easydrive.activities.interficie_usuari.IniciUsuari
import com.example.easydrive.activities.menu.Ajuda
import com.example.easydrive.activities.menu.Configuracio
import com.example.easydrive.activities.menu.HistorialViatges
import com.example.easydrive.activities.menu.Perfil
import com.example.easydrive.adaptadors.AdaptadorRVDestins
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.api.geoapify.CrudGeo
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.rutaDesti
import com.example.easydrive.dades.rutaEscollida
import com.example.easydrive.dades.rutaOrigen
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityIniciTaxistaBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IniciTaxista : AppCompatActivity(), OnNavigationItemSelectedListener , OnMapReadyCallback{
    private lateinit var binding: ActivityIniciTaxistaBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    val REQUESTS_PERMISIONS = 1
    var permisos = false
    var map: GoogleMap? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var controlRecollirClients: Boolean = false

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
        val crud = CrudApiEasyDrive()
        if (user == null){
            user = intent.getSerializableExtra("user") as? Usuari
            editarHeader()
            afegirFoto()
        }
        Log.d("Usuari", user.toString())
        //permisos
        if(comprovarPermisos()){
            permisos = true

        }else{
            demanarPermisos()
        }
        editarHeader()
        afegirFoto()
        getDisponiblitat(crud)

        val handler = Handler(Looper.getMainLooper())
        val intervalMillis: Long = 10000 // cada 10 segundos

        val checkReservesRunnable = object : Runnable {
            override fun run() {
                comprovarNousViatges()
                handler.postDelayed(this, intervalMillis)
            }
        }

// Solo iniciar si está disponible
        if (binding.switchDisponiblitat.isChecked) {
            controlRecollirClients = true
            handler.post(checkReservesRunnable)
        }


        binding.switchDisponiblitat.setOnCheckedChangeListener { _, isChecked ->
            val dispo = isChecked
            val idUsuari = user?.dni ?: return@setOnCheckedChangeListener

                val success = crud.updateDispoTaxista(idUsuari, dispo)

                    if (success) {
                        Toast.makeText(this, "Disponibilitat actualitzada", Toast.LENGTH_SHORT).show()
                        if (dispo) {
                            handler.post(checkReservesRunnable)
                        } else {
                            handler.removeCallbacks(checkReservesRunnable)
                        }
                    } else {
                        Toast.makeText(this, "Error actualitzant disponibilitat", Toast.LENGTH_SHORT).show()
                        binding.switchDisponiblitat.isChecked = !dispo
                    }
        }


        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.main, R.string.obert, R.string.tancat)
        binding.main.addDrawerListener(actionBarDrawerToggle)
        binding.navigator.setNavigationItemSelectedListener(this)

        binding.btnPerfil.setOnClickListener {
            binding.main.openDrawer(GravityCompat.START)
        }
        if (binding.switchDisponiblitat.isChecked){
            //poner cuando llegue un reserva
        }
    }

    private fun getDisponiblitat(crud: CrudApiEasyDrive) {
        if (crud.getDispoTaxista(user?.dni!!)){
            binding.switchDisponiblitat.isChecked = true
        }else{
            binding.switchDisponiblitat.isChecked = false
        }
    }

    override fun onMapReady(googleMap: GoogleMap){
        map = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permisos de ubicación no concedidos", Toast.LENGTH_SHORT).show()
            return
        }

        map?.isMyLocationEnabled = true // Este muestra el icono azul de ubicación

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val ubicacio = LatLng(location.latitude, location.longitude)
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(ubicacio, 15f),
                    2000,
                    null
                )
            } else {
                Toast.makeText(this, "No s'ha pogut obtenir la ubicació", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editarHeader() {
        val headerView = binding.navigator.getHeaderView(0)
        //posar nom
        val nomUser = headerView.findViewById<TextView>(R.id.nomCognomHeader)
        nomUser.text = "${user?.nom} ${user?.cognom}"
        //posar imatge
        var fotoUser = headerView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.fotoHeader)
        Glide.with(this)
            .load("http://172.16.24.115:7126/Photos/${user?.fotoPerfil}")
            .into(fotoUser)
    }

    private fun afegirFoto() {
        Glide.with(this)
            .load("http://172.16.24.115:7126/Photos/${user?.fotoPerfil}")
            .into(binding.btnPerfil)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val menu = binding.navigator.menu
        for (i in 0 until menu.size()) {
            menu.getItem(i).isChecked = false
        }

        item.setChecked(true)
        when(item.itemId){
            R.id.menuHome->{
                startActivity(Intent(this, IniciUsuari::class.java))
            }
            R.id.menuPerfil -> {
                startActivity(Intent(this, Perfil::class.java))
            }
            R.id.menuConfig ->{
                startActivity(Intent(this, Configuracio::class.java))
            }
            R.id.menuHistorial ->{
                startActivity(Intent(this, HistorialViatges::class.java))
            }
            R.id.menuContacte ->{
                startActivity(Intent(this, Ajuda::class.java))
            }

        }

        binding.main.closeDrawer(GravityCompat.START)
        return true
    }
    fun dialogRecollirClient(){
        controlRecollirClients = false
        val dialeg = Dialog(this)
        dialeg.setContentView(R.layout.dialog_recollirclient)
        dialeg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //dialeg.window?.setWindowAnimations(R.style.animation)
        dialeg.setCancelable(false)

        dialeg.findViewById<MaterialButton>(R.id.btnAcceptarD).setOnClickListener { // si acepta la reserva
            controlRecollirClients = false
            dialeg.dismiss()
        }

        dialeg.findViewById<MaterialButton>(R.id.btnCancelarD).setOnClickListener {
            controlRecollirClients = true
            dialeg.dismiss()
        }

        dialeg.show()
    }

    fun comprovarNousViatges(){
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = sdf.parse(sdf.format(Date()))

        val crud = CrudApiEasyDrive()
        val pendents = crud.getReservesPendents()
        val novaPendents = mutableListOf<Reserva>()

        Log.d("pendents", pendents.toString())
        pendents?.forEach { p ->
            val dataReserva = p.dataViatge?.let { sdf.parse(it) }
            if ( dataReserva != null && dataReserva == hoy) {
                novaPendents.add(p)
            }
        }

        if (novaPendents.isNotEmpty()){
            Log.d("novaPendents",novaPendents.toString())
            if (controlRecollirClients){
                runOnUiThread {
                    dialogRecollirClient()
                }
            }
        }
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