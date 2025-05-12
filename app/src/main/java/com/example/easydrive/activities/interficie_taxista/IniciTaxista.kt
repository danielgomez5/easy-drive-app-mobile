package com.example.easydrive.activities.interficie_taxista

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import com.example.easydrive.api.openroute.CrudOpenRoute
import com.example.easydrive.dades.GeoapifyDades
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.rutaDesti
import com.example.easydrive.dades.rutaEscollida
import com.example.easydrive.dades.rutaOrigen
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityIniciTaxistaBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IniciTaxista : AppCompatActivity(), OnNavigationItemSelectedListener , OnMapReadyCallback{
    private lateinit var binding: ActivityIniciTaxistaBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    val REQUESTS_PERMISIONS = 1
    var permisos = false

    var ubicacioActual: LatLng? = null
    var poly: Polyline? = null
    var map: GoogleMap? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    val novaPendents = mutableListOf<Reserva>()

    var controlRecollirClients: Boolean = false
    var rutaDelViajeMostrada = false

    var ubiClient: LatLng?=null
    var destiClient : LatLng?=null

    var reservaXEdit : Reserva?=null

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var checkReservesRunnable: Runnable


    private var isRequestInProgress = false


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            val location = locationResult.lastLocation ?: return

            val conductorLat = location.latitude
            val conductorLng = location.longitude

            // Verifica la distancia hasta el cliente
            val distancia = FloatArray(1)
            ubiClient?.let {
                Location.distanceBetween(
                    conductorLat, conductorLng,
                    it.latitude, it.longitude,
                    distancia
                )
                if (distancia[0] < 50 && !rutaDelViajeMostrada) {
                    rutaDelViajeMostrada = true
                    trazarRutaViaje()
                }
            }

        }
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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

        val intervalMillis: Long = 20000 // cada 10 segundos

        checkReservesRunnable = object : Runnable {
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

        binding.cardInfoClient.visibility = View.GONE

        binding.switchDisponiblitat.setOnCheckedChangeListener { _, isChecked ->
            val dispo = isChecked
            val idUsuari = user?.dni ?: return@setOnCheckedChangeListener
            try {
                val success = crud.updateDispoTaxista(idUsuari, dispo)
                if (success) {
                    Toast.makeText(this, "Disponibilitat actualitzada", Toast.LENGTH_SHORT).show()
                    if (dispo) handler.post(checkReservesRunnable)
                    else handler.removeCallbacks(checkReservesRunnable)
                } else {
                    Toast.makeText(this, "Error actualitzant disponibilitat", Toast.LENGTH_SHORT).show()
                    binding.switchDisponiblitat.isChecked = !dispo
                }
            } catch (e: Exception) {
                Toast.makeText(this, "No s'ha pogut connectar amb el servidor", Toast.LENGTH_LONG).show()
                Log.e("updateDispoTaxista", "Error de connexió", e)
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
                ubicacioActual = LatLng(location.latitude, location.longitude)
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(ubicacioActual!!, 15f),
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
        try{
        Glide.with(this)
            .load("http://172.16.24.115:7126/Photos/${user?.fotoPerfil}")
            .error(R.drawable.logo_easydrive)
            .into(fotoUser)
        }catch (e: Exception){
            fotoUser.setImageResource(R.drawable.logo_easydrive)
        }
    }

    private fun afegirFoto() {
        try{
        Glide.with(this)
            .load("http://172.16.24.115:7126/Photos/${user?.fotoPerfil}")
            .error(R.drawable.logo_easydrive)
            .into(binding.btnPerfil)
        }catch (e: Exception){
            binding.btnPerfil.setImageResource(R.drawable.logo_easydrive)
        }
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

        val reserva = novaPendents.first()

        val tvOrigen = dialeg.findViewById<TextView>(R.id.tvOrigen)
        val tvDesti = dialeg.findViewById<TextView>(R.id.tvDesti)
        val tvDataHora = dialeg.findViewById<TextView>(R.id.tvDataHora)
        val tvPreu = dialeg.findViewById<TextView>(R.id.tvPreu)

        tvOrigen.text = "Origen: ${reserva.origen ?: "Desconegut"}"
        tvDesti.text = "Desti: ${reserva.desti ?: "Desconegut"}"
        tvDataHora.text = "Data i hora: ${reserva.dataViatge ?: ""} ${reserva.horaViatge ?: ""}"
        tvPreu.text = "Preu: ${reserva.preu?.toString() ?: "No disponible"} €"


        dialeg.findViewById<MaterialButton>(R.id.btnAcceptarD).setOnClickListener { // si acepta la reserva
            controlRecollirClients = false
            reservaXEdit = novaPendents.first()
            handler.removeCallbacks(checkReservesRunnable) // Detenemos comprobación de nuevas reservas
            startCheckingReserva() // Comenzamos a comprobar estado de la reserva activa

            val crud = CrudApiEasyDrive()
            if(getReservaNoConfirmada(crud, reservaXEdit!!)){
                reservaXEdit?.idEstat = 1
                crud.changeEstatReserva(reservaXEdit?.id.toString(), reservaXEdit!!)
                val client = crud.getUsuariById(reservaXEdit?.idUsuari.toString())

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@setOnClickListener
                }
                tracarRutaFinsClient(reservaXEdit!!, client!!)
                dialeg.dismiss()
            } else{
                Log.d("ya lo han confirmado", "pues eso")
            }
            dialeg.dismiss()

        }

        dialeg.findViewById<MaterialButton>(R.id.btnCancelarD).setOnClickListener {
            controlRecollirClients = true
            dialeg.dismiss()
        }

        dialeg.show()
    }

    fun getReservaNoConfirmada(crud : CrudApiEasyDrive, reserva: Reserva): Boolean{
        val reserva = crud.getResevraById(reserva?.id.toString())
        Log.d("getReservaNoConfirmada", reserva.toString())
        if (reserva?.idEstat == 2)
            return true
        else
            return false
    }

    fun comprovarNousViatges() {
        if (isRequestInProgress) return
        isRequestInProgress = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val hoy = sdf.parse(sdf.format(Date()))

                val crud = CrudApiEasyDrive()
                val pendents = crud.getReservesPendents()

                pendents?.forEach { p ->
                    val dataReserva = p.dataViatge?.let { sdf.parse(it) }
                    if (dataReserva != null && dataReserva == hoy) {
                        novaPendents.add(p)
                    }
                }

                if (novaPendents.isNotEmpty() && controlRecollirClients) {
                    withContext(Dispatchers.Main) {
                        dialogRecollirClient()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error obtenint reserves: ${e.message}")
            } finally {
                isRequestInProgress = false
            }
        }
    }

    private fun startCheckingReserva() {
        val crud = CrudApiEasyDrive()
        if (reservaXEdit != null) {
            checkReservesRunnable = object : Runnable {
                override fun run() {
                    Thread {
                        val reserva = crud.getResevraById(reservaXEdit!!.id.toString())

                        if (reserva != null) {
                            Log.d("Reserva", "Estado actual: ${reserva.idEstat}")

                            when (reserva.idEstat) {
                                3 -> { // Realizat
                                    poly?.remove()
                                    binding.cardInfoClient.visibility = View.GONE
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(
                                            this@IniciTaxista,
                                            "Viatge finalitzat",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    handler.removeCallbacks(this)
                                }
                                4 -> { // Cancelat
                                    poly?.remove()
                                    binding.cardInfoClient.visibility = View.GONE
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(
                                            this@IniciTaxista,
                                            "La reserva ha estat cancel·lada",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    handler.removeCallbacks(this)
                                }
                                else -> {
                                    // Seguir comprovant
                                    handler.postDelayed(this, 10000)
                                }
                            }
                        } else {
                            Log.d("Reserva", "Reserva no trobada")
                            handler.removeCallbacks(this)
                        }
                    }.start()
                }
            }

            handler.post(checkReservesRunnable)
        }
    }

    private fun drawRoute(gMap: GoogleMap, coordenades: List<List<Double>>) {
        val polylineOptions = PolylineOptions()

        coordenades.forEach {
            polylineOptions.add(LatLng(it[1], it[0]))
        }
        runOnUiThread {
            poly = gMap.addPolyline(polylineOptions)
        }

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun tracarRutaFinsClient(reserva: Reserva, client: Usuari) {
        //startCheckingReserva()
        binding.cardInfoClient.visibility = View.VISIBLE
        val crud = CrudOpenRoute(this)

        ubiClient = ubicacioLatLagByText(reserva.origen.toString()) // esto funciona
        destiClient = ubicacioLatLagByText(reserva.desti.toString())

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        val start = ubicacioActual?.longitude.toString() + "," + ubicacioActual?.latitude.toString()
        val end = ubiClient?.longitude.toString() + "," + ubiClient?.latitude.toString()

        Log.d("start", start.toString())
        Log.d("end", end.toString())

        var coordenada: List<List<Double>>? = null
        var horas: Int? = null
        var minutos: Int? = null
        var segundos: Int? = null

        var resposta = crud.getRutaCotxe(start, end)
        if (resposta != null) {
            resposta.features.map {
                coordenada = it.geometry.coordinates
                horas = (it.properties.summary.duration.toInt() / 3600)
                minutos = ((it.properties.summary.duration.toInt()-horas!!*3600)/60)
                segundos = it.properties.summary.duration.toInt()-(horas!!*3600+minutos!!*60)
                binding.arribada.setText("Arribada en ${horas.toString()}:${minutos.toString()}:${segundos.toString()} per recollir client")
                binding.nomClient.setText(client.nom + " " + client.cognom)
            }
            drawRoute(map!!, coordenada!!)
        } else {
            Log.d("resposta api", resposta.toString())
            Toast.makeText(this, "No hi ha resposta", Toast.LENGTH_LONG)
        }
    }

    fun ubicacioLatLagByText(reserva: String): LatLng?{
        val crudGeo = CrudGeo(this)
        val llocClient = reserva?.replace (" ","+")
        val ubiGeo = crudGeo.getLocationByName(llocClient!!)

        var ubiClientGeo : GeoapifyDades?=null
        ubiGeo.forEach { ubi ->
            val text = "${ubi.address_line1}, ${ubi.city}"
            if (text.equals(reserva)){
                ubiClientGeo = ubi
            }

        }

        return LatLng(ubiClientGeo?.lat?.toDouble()!!, ubiClientGeo?.lon?.toDouble()!!)
    }

    fun trazarRutaViaje() {
        binding.cardInfoClient.visibility = View.INVISIBLE
        poly?.remove()

        val ubiOrg = LatLng(ubiClient!!.latitude,ubiClient!!.longitude)
        Log.d("UbiOrg", ubiOrg.toString())
        val ubiDest = LatLng(destiClient!!.latitude,destiClient!!.longitude)
        Log.d("UbiDest", ubiDest.toString())

        val crud = CrudOpenRoute(this)
        val start = ubiOrg.longitude.toString() + "," + ubiOrg.latitude.toString()
        val end = ubiDest.longitude.toString() + "," + ubiDest.latitude.toString()

        Log.d("start", start.toString())
        Log.d("end", end.toString())

        var coordenada: List<List<Double>>? = null
        var horas: Int? = null
        var minutos: Int? = null
        var segundos: Int? = null

        var resposta = crud.getRutaCotxe(start, end)
        if (resposta != null) {
            resposta.features.map {
                coordenada = it.geometry.coordinates
                horas = (it.properties.summary.duration.toInt() / 3600)
                minutos = ((it.properties.summary.duration.toInt()-horas!!*3600)/60)
                segundos = it.properties.summary.duration.toInt()-(horas!!*3600+minutos!!*60)
            }
            drawRoute(map!!, coordenada!!)
        } else {
            Log.d("resposta api", resposta.toString())
            Toast.makeText(this, "No hi ha resposta", Toast.LENGTH_LONG)
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