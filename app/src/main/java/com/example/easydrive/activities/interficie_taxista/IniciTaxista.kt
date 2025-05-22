package com.example.easydrive.activities.interficie_taxista

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.easydrive.R
import com.example.easydrive.activities.MainActivity
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
import com.example.easydrive.dades.viatja
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
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

    var ubiClient: LatLng?=null
    var destiClient : LatLng?=null
    private var marcadorSimulacio: Marker? = null
    private var marcadorFinal: Marker? = null

    val novaPendents = mutableListOf<Reserva>()
    var reservaXEdit : Reserva?=null
    var cotxe : Cotxe?=null
    var cotxesByTaxi : List<Cotxe>?=null
    var destFinal: String?=null
    var estat5 : Boolean =false
    private var arribatAlDesti = false

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var checkReservesRunnable: Runnable
    private var isRequestInProgress = false
    private var checkingReservaRunning = false

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
        viatja = Viatja(null,null,null,null,null,null,null,null,null, null)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1002
            )
        }

        crearCanalNotificacions()
        editarHeader()
        afegirFoto()
        getDisponiblitat(crud)
        runReserves()

        val prefs = getSharedPreferences("configuracio", MODE_PRIVATE)
        val editor = prefs.edit()

        val modeOscur = prefs.getBoolean("mode_oscuro", false)
        binding.switchMode.isChecked = modeOscur

        fun updateSwitchIcon(isNight: Boolean) {
            val drawableRes = if (isNight) R.drawable.baseline_dark_mode_24 else R.drawable.baseline_sunny_24
            binding.switchMode.thumbDrawable = ContextCompat.getDrawable(this, drawableRes)
        }

        updateSwitchIcon(modeOscur)

        AppCompatDelegate.setDefaultNightMode(
            if (modeOscur) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        binding.switchMode.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            editor.putBoolean("mode_oscuro", isChecked)
            editor.apply()

            updateSwitchIcon(isChecked)

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }


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
        binding.navigator.menu.findItem(R.id.menuConfig).isVisible = false

        binding.btnPerfil.setOnClickListener {
            binding.main.openDrawer(GravityCompat.START)
        }

        binding.simulacio.setOnClickListener {
            binding.simulacio.visibility = View.GONE
            mostrarDialegClientPujat {
                simulacioRutaArribarDesti()
            }
        }
    }


    private fun runReserves() {
        val intervalMillis: Long = 20000 // cada 10 segundos
        checkReservesRunnable = object : Runnable {
            override fun run() {
                comprovarNousViatges()
                handler.postDelayed(this, intervalMillis)
            }
        }
    }

    private fun crearCanalNotificacions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "RECOLLIDA_CLIENTS",
                "Notificacions de recollida",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal per notificar clients a recollir"
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(canal)
        }
    }

    fun crearPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun enviarNotificacioClient(context: Context) {
        val builder = NotificationCompat.Builder(context, "RECOLLIDA_CLIENTS")
            .setSmallIcon(R.drawable.baseline_circle_notifications_24) // Asegúrate de tener un icono válido
            .setContentTitle("Atenció!")
            .setContentText("Hi ha un client a recollir.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(crearPendingIntent(context))
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

            with(NotificationManagerCompat.from(context)) {
                notify(1001, builder.build())
            }
        } else {
            Log.w("NOTIFICATION", "Permís de notificació no concedit.")
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

        val crud = CrudApiEasyDrive()

        try{
            user?.fotoPerfil = crud.getImagePerfName(user?.dni!!)
        }catch (e: Exception){

        }

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

        val crud = CrudApiEasyDrive()

        try{
            user?.fotoPerfil = crud.getImagePerfName(user?.dni!!)
        }catch (e: Exception){

        }

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

        dialeg.findViewById<MaterialButton>(R.id.btnAcceptarD).setOnClickListener {
            controlRecollirClients = false
            reservaXEdit = novaPendents.removeFirstOrNull()
            handler.removeCallbacks(checkReservesRunnable)
            startCheckingReserva()

            val crud = CrudApiEasyDrive()
            if (getReservaNoConfirmada(crud, reservaXEdit!!)) {
                reservaXEdit?.idEstat = 1
                reservaXEdit?.estat = "OK"
                crud.changeEstatReserva(reservaXEdit?.id.toString(), reservaXEdit!!)
                val client = crud.getUsuariById(reservaXEdit?.idUsuari.toString())

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@setOnClickListener
                }

                cotxesByTaxi = crud.getAllCotxesByUsuari(user?.dni!!)
                if (cotxesByTaxi?.size == 1) {
                    cotxe = cotxesByTaxi?.first()
                    iniciarRutaSegonsHora(client!!)
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
                        if (cotxeSeleccionat != null) {
                            cotxe = cotxeSeleccionat
                            iniciarRutaSegonsHora(client!!)
                            dialegCotxe.dismiss()
                            dialeg.dismiss()
                        } else {
                            Toast.makeText(this, "Has de seleccionar un cotxe", Toast.LENGTH_SHORT).show()
                        }
                    }

                    dialegCotxe.show()
                }

                val viatgeIncomplet = Viatja(
                    id = null,
                    durada = null,
                    distancia = null,
                    valoracio = null,
                    comentari = null,
                    idZona = null,
                    idTaxista = user?.dni,
                    idReserva = reservaXEdit?.id,
                    idCotxe = cotxe?.matricula,
                    idReservaNavigation = null
                )

                if (crud.insertViatge(viatgeIncomplet)) {
                    Log.d("viatge temporal insertat", "CORRECTE")
                } else {
                    Log.d("viatge temporal insertat", "INCORRECTE")
                }
            } else {
                Log.d("ya lo han confirmado", "pues eso")
            }

            dialeg.dismiss()
        }

        dialeg.findViewById<MaterialButton>(R.id.btnCancelarD).setOnClickListener {
            novaPendents.removeFirstOrNull()
            controlRecollirClients = true
            dialeg.dismiss()
        }

        dialeg.show()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun iniciarRutaSegonsHora(client: Usuari) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dataHoraReserva = sdf.parse("${reservaXEdit?.dataViatge} ${reservaXEdit?.horaViatge}")
        val ara = Date()

        if (dataHoraReserva != null && dataHoraReserva.after(ara)) {
            Log.d("Reserva", "És una reserva programada. Esperant hora...")
            esperarMomentReserva(dataHoraReserva, reservaXEdit!!, client)
        } else {
            tracarRutaFinsClient(reservaXEdit!!, client)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun esperarMomentReserva(hora: Date, reserva: Reserva, client: Usuari) {
        val delayMillis = hora.time - System.currentTimeMillis()
        handler.postDelayed({
            tracarRutaFinsClient(reserva, client)
        }, delayMillis)
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

                novaPendents.clear()
                pendents?.forEach { p ->
                    val dataReserva = p.dataViatge?.let { sdf.parse(it) }
                    if (dataReserva != null && dataReserva == hoy) {
                        novaPendents.add(p)
                    }
                }

                if (novaPendents.isNotEmpty() && controlRecollirClients) {
                    withContext(Dispatchers.Main) {
                        dialogRecollirClient()
                        enviarNotificacioClient(this@IniciTaxista)
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error obtenint reserves: ${e.message}")
            } finally {
                isRequestInProgress = false
            }
        }
    }

    private var checkingReservaJob: Job? = null

    private fun startCheckingReserva() {
        val crud = CrudApiEasyDrive()
        if (reservaXEdit != null && checkingReservaJob == null) {
            checkingReservaRunning = true

            checkingReservaJob = lifecycleScope.launch(Dispatchers.IO) {
                while (isActive) {
                    val reserva = crud.getResevraById(reservaXEdit!!.id.toString())

                    if (estat5) {
                        reserva?.idEstat = 5
                    }

                    withContext(Dispatchers.Main) {
                        if (reserva != null) {
                            Log.d("Reserva", "Estado actual: ${reserva.idEstat}")

                            when (reserva.idEstat) {
                                3, 5 -> {
                                    poly?.remove()
                                    binding.cardInfoClient.visibility = View.GONE
                                    Toast.makeText(
                                        this@IniciTaxista,
                                        "Arribada a la ubicació d'origen",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    if (reserva.idEstat == 5) {
                                        trazarRutaViaje()
                                    }
                                    checkingReservaJob?.cancel()
                                    checkingReservaRunning = false
                                }

                                4 -> {
                                    poly?.remove()
                                    binding.cardInfoClient.visibility = View.GONE
                                    Toast.makeText(
                                        this@IniciTaxista,
                                        "La reserva ha estat cancel·lada",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    checkingReservaJob?.cancel()
                                    checkingReservaRunning = false
                                }

                                else -> {
                                    // Esperar 10 segundos para volver a comprobar
                                }
                            }
                        } else {
                            Log.d("Reserva", "Reserva no trobada")
                            checkingReservaJob?.cancel()
                            checkingReservaRunning = false
                        }
                    }

                    delay(10_000L) // 10 segundos
                }
            }
        }
    }

    private fun drawRoute(gMap: GoogleMap, coordenades: List<List<Double>>) {
        poly?.remove()
        val polylineOptions = PolylineOptions().color(R.color.md_theme_inversePrimary_mediumContrast_night)
            .width(10f)
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

        val start = ubicacioActual?.longitude.toString() + "," + ubicacioActual?.latitude.toString()
        val end = ubiClient?.longitude.toString() + "," + ubiClient?.latitude.toString()
        marcadorFinal = map?.addMarker(MarkerOptions().position(ubiClient!!))
        Log.d("start", start.toString())
        Log.d("end", end.toString())

        var horas: Int? = null
        var minutos: Int? = null
        var segundos: Int? = null

        try{
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

                lifecycleScope.launch(Dispatchers.Default) {
                    drawRoute(map!!, coordenadesViatgeClient!!)  // Correcto, porque estamos en una coroutine
                }
                drawRoute(map!!, coordenadesViatgeClient!!)
                poly?.remove()
                simularRuta()
                //simularRutaUnificada(false)
                //simulacioRutaArribarDesti()
            } else {
                Log.d("resposta api", resposta.toString())
                Toast.makeText(this, "No hi ha resposta", Toast.LENGTH_LONG)
            }
        }catch(e: SocketTimeoutException) {
            Log.e("RutaError", "Timeout en la petició a OpenRouteService")
            Toast.makeText(this, "Error de temps d'espera en la ruta", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("RutaError", "Error inesperat: ${e.message}")
            Toast.makeText(this, "Error inesperat en la ruta", Toast.LENGTH_LONG).show()
        }

    }
    private var simulacioJob: Job? = null

    fun simularRuta() {
        simulacioJob?.cancel() // Cancelar si ya hay uno corriendo

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
        /*val puntFinal = coordenades.last()

        val latLngFinal= LatLng(puntFinal[1], puntFinal[0])
        marcadorFinal = map?.addMarker(MarkerOptions().position(latLngFinal))*/

        simulacioJob = CoroutineScope(Dispatchers.Main).launch {
            while (coordenades.isNotEmpty()) {
                poly?.remove()
                drawRoute(map!!, coordenades)
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

                if (indexStep < steps.size) {
                    val currentStep = steps[indexStep]
                    val (start, end) = currentStep.way_points

                    if (indexPunt >= start && indexPunt <= end) {
                        indexStep++
                    }

                    if (currentStep.instruction.contains("arrive", ignoreCase = true)) {
                        Log.d("SIMULACIO", "Detectat pas final per instrucció: ${currentStep.instruction}")
                    }
                }

                indexPunt++
                delay(1000L) // espera 1 segundo entre puntos
            }

            withContext(Dispatchers.Main) {
                Log.d("SIMULACIO", "Verificació de distància forçada al final")
                verificarDistancia(ubicacioActual!!)
            }
        }
    }

    private fun mostrarDialegClientPujat(onConfirmar: () -> Unit) {
        val dialeg = Dialog(this)
        dialeg.setContentView(R.layout.dialog_client_pujat)
        dialeg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialeg.setCancelable(false)

        val btnConfirmar = dialeg.findViewById<MaterialButton>(R.id.btnClientPujat)
        val btnCancelar = dialeg.findViewById<MaterialButton>(R.id.btnCancelPujat)

        btnConfirmar.setOnClickListener {
            dialeg.dismiss()
            onConfirmar()
        }

        btnCancelar.setOnClickListener {
            Toast.makeText(this, "Espera fins que el client estigui al vehicle", Toast.LENGTH_SHORT).show()
        }

        dialeg.show()
    }

    fun verificarDistancia(posicionActual: LatLng) {
        Log.d("VERIFICAR", "ubicacioActual: $posicionActual, ubiClient: $ubiClient")

        ubiClient?.let {
            val distancia = FloatArray(1)
            Location.distanceBetween(
                posicionActual.latitude, posicionActual.longitude,
                it.latitude, it.longitude,
                distancia
            )
            Log.d("VERIFICAR", "Distancia al client: ${distancia[0]}")

            if (distancia[0] < 30 && !rutaDelViajeMostrada) {
                Log.d("VERIFICAR", "Distancia < 10m -> estat5=true")
                rutaDelViajeMostrada = true
                estat5 = true
                marcadorFinal?.remove()
                simulacioJob?.cancel()
            }
        } ?: Log.e("VERIFICAR", "ubiClient és NULL")
    }


    fun ubicacioLatLagByText(reserva: String): LatLng?{
        val crudGeo = CrudGeo(this)
        val ubiGeo = crudGeo.getLocationByName(reserva!!)

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
        binding.cardDispo.visibility = View.GONE
        binding.switchMode.visibility = View.GONE
        binding.btnExpandMenu.visibility = View.GONE
        poly?.remove()
        marcadorFinal?.remove()

        val ubiOrg = LatLng(ubiClient!!.latitude, ubiClient!!.longitude)
        Log.d("UbiOrg", ubiOrg.toString())
        val ubiDest = LatLng(destiClient!!.latitude, destiClient!!.longitude)
        Log.d("UbiDest", ubiDest.toString())
        marcadorFinal = map?.addMarker(MarkerOptions().position(ubiDest))
        val crudRuta = CrudOpenRoute(this)
        val start = "${ubiOrg.longitude},${ubiOrg.latitude}"
        val end = "${ubiDest.longitude},${ubiDest.latitude}"
        destFinal = end
        Log.d("start", start)
        Log.d("end", end)

        var segundos: Int? = null
        var resposta = crudRuta.getRutaCotxe(start, end)

        if (resposta != null) {
            resposta.features.map {
                coordenadesViatgeClient = it.geometry.coordinates as MutableList<List<Double>>?
                segundos = it.properties.summary.duration.toInt()
                it.properties.segments.map { segment ->
                    instruccio = segment.steps
                }
            }

            val crud = CrudApiEasyDrive()

            // Recuperar el viatge existente por ID de reserva
            val viatgeExist = crud.getViatgeByReserva(reservaXEdit?.id!!)
            if (viatgeExist != null) {
                viatgeExist.distancia = resposta.features.first().properties.summary.distance / 1000
                viatgeExist.durada = segundos?.div(60)
                viatgeExist.idZona = user?.idZona

                // Actualizar horas del coche
                cotxe?.horesTreballades = cotxe?.horesTreballades?.plus(segundos?.toFloat()!!.div(60))

                // Actualizar el viaje
                if (crud.updateViatge(viatgeExist.id!!, viatgeExist)) {
                    Log.d("update Viatge", "correcte")
                } else {
                    Log.d("update Viatge", "incorrecte")
                }

                // Actualizar el coche
                if (cotxe != null && crud.updateCotxe(cotxe?.matricula!!, cotxe!!)) {
                    Log.d("update cotxe", "correcte")
                } else {
                    Log.d("update cotxe", "incorrecte")
                }

                drawRoute(map!!, coordenadesViatgeClient!!)
            } else {
                Log.e("Viatge error", "No s'ha trobat cap viatge per aquesta reserva.")
                Toast.makeText(this, "Error: no s'ha trobat el viatge", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d("resposta api", resposta.toString())
            Toast.makeText(this, "No hi ha resposta", Toast.LENGTH_LONG).show()
        }
    }


    private var simulacioRutaJob: Job? = null





    private fun simulacioRutaArribarDesti() {
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

        var final = false

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
                        if (currentStep.instruction.contains("Arrive")){
                            final = true
                        }
                    }
                }

                coordenades.removeAt(0)
                indexPunt++
                delay(1000)
            }
            withContext(Dispatchers.Main) {
                if (final) {
                    Log.d("SIMULACIO", "Arribat al destí, obrint diàleg")
                    verificarDistanciaDestiFinal(ubicacioActual!!)
                } else {
                    Log.d("SIMULACIO", "No s'ha detectat pas final")
                }
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

            if (distancia[0] < 30  && !arribatAlDesti) {
                val crud = CrudApiEasyDrive()
                arribatAlDesti = true  // Marca como "ya ha llegado"
                binding.cardDispo.visibility = View.GONE
                binding.btnExpandMenu.visibility = View.GONE
                reservaXEdit?.idEstat =3
                marcadorFinal?.remove()
                crud.changeEstatReserva(reservaXEdit?.id.toString(), reservaXEdit!!)
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
        val crud = CrudApiEasyDrive()
        val dialeg = Dialog(this@IniciTaxista)
        dialeg.setContentView(R.layout.dialeg_ruta_finalitzada_taxi)
        dialeg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //dialeg.window?.setWindowAnimations(R.style.animation)
        dialeg.setCancelable(false)

        dialeg.findViewById<ImageButton>(R.id.btnTanca).setOnClickListener {
            reservaXEdit?.idEstat = 3
            crud.changeEstatReserva(reservaXEdit?.id.toString(), reservaXEdit!!)
            //poner un crud de updateReserva
            binding.indicacions.visibility = View.GONE
            binding.cardDispo.visibility = View.VISIBLE
            binding.switchMode.visibility = View.VISIBLE
            binding.btnExpandMenu.visibility = View.VISIBLE
            runReserves()
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

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
    }

    override fun onDestroy() {
        simulacioRutaJob?.cancel()
        super.onDestroy()
    }
}