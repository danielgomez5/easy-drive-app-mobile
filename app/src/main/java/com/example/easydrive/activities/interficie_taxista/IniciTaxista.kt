package com.example.easydrive.activities.interficie_taxista

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.example.easydrive.adaptadors.AdaptadorEscollirCotxe
import com.example.easydrive.adaptadors.AdaptadorRVDestins
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.api.geoapify.CrudGeo
import com.example.easydrive.api.openroute.CrudOpenRoute
import com.example.easydrive.dades.Cotxe
import com.example.easydrive.dades.GeoapifyDades
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.Step
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.Viatja
import com.example.easydrive.dades.cotxeSeleccionat
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.Double
import kotlin.collections.List

class IniciTaxista : AppCompatActivity(), OnNavigationItemSelectedListener , OnMapReadyCallback{
    private lateinit var binding: ActivityIniciTaxistaBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private var menuVisible = false
    val REQUESTS_PERMISIONS = 1
    var permisos = false

    var ubicacioActual: LatLng? = null
    var poly: Polyline? = null
    var map: GoogleMap? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var coordenadesViatgeClient: MutableList<List<Double>>? = null
    var instruccio : List<Step>? = null

    var controlRecollirClients: Boolean = false
    var rutaDelViajeMostrada = false
    var rutaAlDestiMostrada = false


    var ubiClient: LatLng?=null
    var destiClient : LatLng?=null
    private var marcadorSimulacio: Marker? = null

    val novaPendents = mutableListOf<Reserva>()
    var reservaXEdit : Reserva?=null
    var viatja : Viatja?=null
    var cotxe : Cotxe?=null
    var cotxesByTaxi : List<Cotxe>?=null
    var destFinal: String?=null
    var estat5 : Boolean =false
    private var arribatAlDesti = false

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
                    estat5 = true
                    trazarRutaViaje()

                }
            }

        }
    }

    private val locationCallbackArribadaDesti = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            val location = locationResult.lastLocation ?: return
            val conductorLat = location.latitude
            val conductorLng = location.longitude

            destiClient?.let {
                val distancia = FloatArray(1)
                Location.distanceBetween(
                    conductorLat, conductorLng,
                    it.latitude, it.longitude,
                    distancia
                )

                if (distancia[0] < 30 && !rutaAlDestiMostrada && !arribatAlDesti) {
                    arribatAlDesti = true  // Marca como "ya ha llegado"
                    rutaAlDestiMostrada = true
                    dialogArribadaDesti()
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
        viatja = Viatja(null,null,null,null,null,null,null,null,null)
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

        val btnExpandMenu = binding.btnExpandMenu
        val tvCotxes = binding.flCotxesRegistrats

        btnExpandMenu.setOnClickListener {
            menuVisible = !menuVisible
            if (menuVisible) {
                tvCotxes.visibility = View.VISIBLE
                tvCotxes.alpha = 0f
                tvCotxes.animate().alpha(1f).setDuration(200).start()
            } else {
                tvCotxes.animate().alpha(0f).setDuration(200).withEndAction {
                    tvCotxes.visibility = View.GONE
                }.start()
            }
        }

        tvCotxes.setOnClickListener {
            val intent = Intent(this, CotxesRegistrats::class.java)
            startActivity(intent)
        }


// Solo iniciar si está disponible
        if (binding.switchDisponiblitat.isChecked) {
            controlRecollirClients = true
            handler.post(checkReservesRunnable)
        }

        binding.cardInfoClient.visibility = View.GONE
        binding.simulacio.visibility = View.GONE

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

        //Mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        //Menu Lateral
        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.main, R.string.obert, R.string.tancat)
        binding.main.addDrawerListener(actionBarDrawerToggle)
        binding.navigator.setNavigationItemSelectedListener(this)

        binding.btnPerfil.setOnClickListener {
            binding.main.openDrawer(GravityCompat.START)
        }

        binding.simulacio.setOnClickListener {
            simulacioRutaArribarDesti()
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

        map?.isMyLocationEnabled = false // Este muestra el icono azul de ubicación

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                ubicacioActual = LatLng(location.latitude, location.longitude)
                marcadorSimulacio = map?.addMarker(
                    MarkerOptions()
                        .position(ubicacioActual!!)
                        .title("Simulació Ubi actual")
                        .icon(BitmapDescriptorFactory.fromBitmap(returnBitmap()))
                )
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
                cotxesByTaxi = crud.getAllCotxesByUsuari(user?.dni!!)
                if (cotxesByTaxi?.size == 1) {
                    cotxe = cotxesByTaxi?.first()
                    tracarRutaFinsClient(reservaXEdit!!, client!!)
                    dialeg.dismiss()
                } else {
                    val dialegCotxe = Dialog(this)
                    dialegCotxe.setContentView(R.layout.dialog_escollir_cotxe)
                    dialegCotxe.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    val recyclerView = dialegCotxe.findViewById<RecyclerView>(R.id.recyclerViewCars)
                    val btnAcceptarCotxe = dialegCotxe.findViewById<MaterialButton>(R.id.btnAcceptarCotxe)

                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = AdaptadorEscollirCotxe(cotxesByTaxi!!)


                    btnAcceptarCotxe.setOnClickListener {
                        Log.d("cotxeSele", "Total cotxes: ${cotxeSeleccionat.toString()}")
                        if (cotxeSeleccionat != null) {
                            cotxe = cotxeSeleccionat
                            tracarRutaFinsClient(reservaXEdit!!, client!!)
                            dialegCotxe.dismiss()
                            dialeg.dismiss()
                        } else {
                            Toast.makeText(this, "Has de seleccionar un cotxe", Toast.LENGTH_SHORT).show()
                        }
                    }

                    dialegCotxe.show()
                }
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

                        if (estat5){
                            reserva?.idEstat = 5
                        }
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
                                5 -> { // Realizat
                                    //poly?.remove()
                                    runOnUiThread {
                                        binding.cardInfoClient.visibility = View.GONE
                                    }
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(
                                            this@IniciTaxista,
                                            "Viatge finalitzat",
                                            Toast.LENGTH_SHORT
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
        poly?.remove()
        val polylineOptions = PolylineOptions().color(Color.BLUE)        // Cambia el color si quieres
            .width(10f)               // Grosor de la línea
            .geodesic(true)

        coordenades.forEach {
            polylineOptions.add(LatLng(it[1], it[0]))
        }
        runOnUiThread {
            poly = gMap.addPolyline(polylineOptions)
        }

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun tracarRutaFinsClient(reserva: Reserva, client: Usuari) {
        binding.cardInfoClient.visibility = View.VISIBLE
        binding.simulacio.visibility = View.VISIBLE
        val crud = CrudOpenRoute(this)

        ubiClient = ubicacioLatLagByText(reserva.origen.toString()) // esto funciona
        destiClient = ubicacioLatLagByText(reserva.desti.toString())

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        //fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        val start = ubicacioActual?.longitude.toString() + "," + ubicacioActual?.latitude.toString()
        val end = ubiClient?.longitude.toString() + "," + ubiClient?.latitude.toString()

        Log.d("start", start.toString())
        Log.d("end", end.toString())

        var horas: Int? = null
        var minutos: Int? = null
        var segundos: Int? = null

        var resposta = crud.getRutaCotxe(start, end)
        if (resposta != null) {
            resposta.features.map {
                coordenadesViatgeClient = it.geometry.coordinates as MutableList<List<Double>>?
                horas = (it.properties.summary.duration.toInt() / 3600)
                minutos = ((it.properties.summary.duration.toInt()-horas!!*3600)/60)
                segundos = it.properties.summary.duration.toInt()-(horas!!*3600+minutos!!*60)
                binding.arribada.setText("Arribada en ${horas.toString()}:${minutos.toString()}:${segundos.toString()} per recollir client")
                binding.nomClient.setText(client.nom + " " + client.cognom)
            }

            drawRoute(map!!, coordenadesViatgeClient!!)
            simularRuta()
            //simulacioRutaArribarDesti()
        } else {
            Log.d("resposta api", resposta.toString())
            Toast.makeText(this, "No hi ha resposta", Toast.LENGTH_LONG)
        }
    }

    fun simularRuta() {
        poly?.remove()
        marcadorSimulacio?.remove()

        if (coordenadesViatgeClient.isNullOrEmpty()) {
            Toast.makeText(this, "No hi ha coordenades de ruta", Toast.LENGTH_SHORT).show()
            return
        }

        val coordenades = coordenadesViatgeClient!!.toMutableList()

        val handler = Handler(Looper.getMainLooper())
        var indexPunt = 0
        var indexStep = 0

        val runnable = object : Runnable {
            override fun run() {
                if (coordenades.isEmpty()) return

                val punt = coordenades.removeAt(0)
                val posicio = LatLng(punt[1], punt[0])
                ubicacioActual = posicio

                if (marcadorSimulacio == null) {
                    marcadorSimulacio = map?.addMarker(
                        MarkerOptions()
                            .position(posicio)
                            .title("Simulació")
                            .icon(BitmapDescriptorFactory.fromBitmap(returnBitmap()))
                    )
                } else {
                    marcadorSimulacio?.position = posicio
                }

                map?.animateCamera(CameraUpdateFactory.newLatLng(posicio))
                drawRoute(map!!, coordenades)

                verificarDistanciaDestiFinal(posicio)

                indexPunt++
                handler.postDelayed(this, 1000) // esperar 1 segundo entre puntos
            }
        }

        handler.post(runnable)
    }


    fun verificarDistancia(posicionActual: LatLng) {
        ubiClient?.let {
            val distancia = FloatArray(1)
            Location.distanceBetween(
                posicionActual.latitude, posicionActual.longitude,
                it.latitude, it.longitude,
                distancia
            )
            if (distancia[0] < 50 && !rutaDelViajeMostrada) {
                rutaDelViajeMostrada = true
                estat5 = true
                trazarRutaViaje() // Aquí ejecutas lo que toca al llegar
            }
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
        destFinal = end
        Log.d("start", start.toString())
        Log.d("end", end.toString())

        var horas: Int? = null
        var minutos: Int? = null
        var segundos: Int? = null


        var resposta = crud.getRutaCotxe(start, end)
        if (resposta != null) {
            resposta.features.map {
                coordenadesViatgeClient = it.geometry.coordinates as MutableList<List<Double>>?
                horas = (it.properties.summary.duration.toInt() / 3600)
                minutos = ((it.properties.summary.duration.toInt()-horas!!*3600)/60)
                segundos = it.properties.summary.duration.toInt()-(horas!!*3600+minutos!!*60)
                viatja?.distancia = (it.properties.summary.distance / 1000).toDouble()
                it.properties.segments.map {
                    instruccio = it.steps
                }
            }
            val crud = CrudApiEasyDrive()

            viatja?.idTaxista = user?.dni
            viatja?.durada = segundos
            viatja?.idZona = user?.idZona
            viatja?.idReserva = reservaXEdit?.id
            viatja?.idCotxe = cotxe?.matricula
            cotxe?.horesTreballades = cotxe?.horesTreballades?.plus(segundos?.toDouble()!!)
            if (crud.insertViatge(viatja!!))
                Log.d("insert Viatge", "correcte")
            else
                Log.d("insert Viatge", "incorrecte")

            if (crud.updateCotxe(cotxe?.matricula!!, cotxe!!))
                Log.d("update cotxe", "correcte")
            else
                Log.d("update cotxe", "incorrecte")

            drawRoute(map!!, coordenadesViatgeClient!!)

        } else {
            Log.d("resposta api", resposta.toString())
            Toast.makeText(this, "No hi ha resposta", Toast.LENGTH_LONG)
        }
    }

    private fun simulacioRutaArribarDesti() {
        poly?.remove()
        marcadorSimulacio?.remove()

        if (coordenadesViatgeClient.isNullOrEmpty()) {
            Toast.makeText(this, "No hi ha coordenades de ruta", Toast.LENGTH_SHORT).show()
            return
        }

        val coordenades = coordenadesViatgeClient!!.toMutableList()
        val steps = instruccio ?: emptyList()
        var indexPunt = 0
        var indexStep = 0

        val primerPunt = coordenades.first()
        val latLngInicial = LatLng(primerPunt[1], primerPunt[0])

        marcadorSimulacio = map?.addMarker(
            MarkerOptions()
                .position(latLngInicial)
                .title("Simulació")
                .icon(BitmapDescriptorFactory.fromBitmap(returnBitmap()))
        )


        CoroutineScope(Dispatchers.Default).launch {
            while (coordenades.isNotEmpty()) {
                val punt = coordenades.first()
                var duration : Double?=null
                withContext(Dispatchers.Main) {
                    val posicio = LatLng(punt[1], punt[0])
                    ubicacioActual = LatLng(punt[1], punt[0])
                    marcadorSimulacio?.position = posicio
                    map?.animateCamera(CameraUpdateFactory.newLatLng(posicio))
                    drawRoute(map!!, coordenades)

                    // Mostrar instrucción si estamos en un nuevo step
                    if (indexStep < steps.size) {
                        val currentStep = steps[indexStep]
                        val (start, end) = currentStep.way_points

                        duration = currentStep.duration
                        if (indexPunt >= start && indexPunt <= end) {
                            // Mostrar instrucción una vez
                            mostrarInstruccio(currentStep.instruction, currentStep.name)
                            indexStep++
                        }
                    }
                }

                    verificarDistanciaDestiFinal(ubicacioActual!!)

                duration!! *100
                coordenades.removeAt(0)
                indexPunt++
                //delay((duration!! *100).toLong())
                delay(1000)
            }
        }
    }
    private fun mostrarInstruccio(instruccio: String, carrer: String) {
        binding.indicacions.visibility = View.VISIBLE
        binding.txtInstruccio.text = instruccio
        binding.txtCarrer.text = carrer
    }

    fun verificarDistanciaDestiFinal(posicionActual: LatLng) {
        destiClient?.let {
            val distancia = FloatArray(1)
            Location.distanceBetween(
                posicionActual.latitude, posicionActual.longitude,
                it.latitude, it.longitude,
                distancia
            )

            if (distancia[0] < 30 && !rutaAlDestiMostrada && !arribatAlDesti) {
                arribatAlDesti = true  // Marca como "ya ha llegado"
                rutaAlDestiMostrada = true
                dialogArribadaDesti()
            }
        }
    }

    fun returnBitmap(): Bitmap{
        return getBitmapFromVectorDrawable(R.drawable.baseline_directions_car_24)
    }

    fun getBitmapFromVectorDrawable(@DrawableRes drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(this, drawableId) ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun dialogArribadaDesti(){
        val dialeg = Dialog(this)
        dialeg.setContentView(R.layout.dialog_recollirclient)
        dialeg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //dialeg.window?.setWindowAnimations(R.style.animation)
        dialeg.setCancelable(false)
        fusedLocationProviderClient.removeLocationUpdates(locationCallbackArribadaDesti)

        dialeg.findViewById<ImageButton>(R.id.btnTanca).setOnClickListener {
            dialeg.dismiss()
        }

        dialeg.show()
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