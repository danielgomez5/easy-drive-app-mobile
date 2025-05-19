package com.example.easydrive.activities.interficie_usuari

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.easydrive.R
import com.example.easydrive.activities.menu.Ajuda
import com.example.easydrive.activities.menu.Configuracio
import com.example.easydrive.activities.menu.HistorialViatges
import com.example.easydrive.activities.menu.Perfil
import com.example.easydrive.adaptadors.AdaptadorEscollirCotxe
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.api.geoapify.CrudGeo
import com.example.easydrive.dades.Globals.clientUbi
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.cotxeSeleccionat
import com.example.easydrive.dades.reservaConf
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityIniciUsuariBinding
import com.example.easydrive.fragments.HomeUsuari
import com.example.easydrive.fragments.ViatgesGuardats
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IniciUsuari : AppCompatActivity() , OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityIniciUsuariBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    val REQUESTS_PERMISIONS = 1
    var permisos = false

    private lateinit var checkReservesRunnable: Runnable
    val novaConfirmat = mutableListOf<Reserva>()
    var resConf: Reserva?=null

    private var isRequestInProgress = false
    private var lastReservaNotificadaId: Int? = null
    private val handler = Handler(Looper.getMainLooper())
    private var arrivedCheckRunnable: Runnable? = null


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
        afegirFoto()
        editarHeader()
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

        val selectedFragment = intent.getIntExtra("SELECTED_FRAGMENT", R.id.menuInici)
        val mostrarSnackbar = intent.getBooleanExtra("MOSTRAR_SNACKBAR", false)

        val fragment = if (selectedFragment == R.id.menuDestinsGuardats) {
            ViatgesGuardats().apply {
                arguments = Bundle().apply {
                    putBoolean("MOSTRAR_SNACKBAR", mostrarSnackbar)
                }
            }
        } else {
            HomeUsuari()
        }

        canviaFragment(fragment)
        binding.bnv.selectedItemId = selectedFragment

        val intervalMillis: Long = 20000 // cada 10 segundos

        checkReservesRunnable = object : Runnable {
            override fun run() {
                comrpovarConfirmat()
                handler.postDelayed(this, intervalMillis)
            }
        }
        handler.post(checkReservesRunnable)
    }
    private val reservesNotificades = mutableSetOf<Int>()
    private var isCheckPaused = false

    fun comrpovarConfirmat() {
        if (isRequestInProgress) return
        isRequestInProgress = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val hoy = sdf.parse(sdf.format(Date()))
                val crud = CrudApiEasyDrive()
                val estatOk = crud.getReservaConf2(user?.dni!!)

                if (!estatOk.isNullOrEmpty()) {
                    for (p in estatOk) {
                        val dataReserva = p.dataViatge?.let { sdf.parse(it) }
                        if (dataReserva != null &&
                            sdf.format(dataReserva) == sdf.format(hoy) &&
                            !reservesNotificades.contains(p.id)
                        ) {
                            reservesNotificades.add(p.id!!)
                            withContext(Dispatchers.Main) {
                                isCheckPaused = true
                                dialogConfirmat(p)
                            }
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error obtenint reserves: ${e.message}")
            } finally {
                isRequestInProgress = false
            }
        }
    }

    private fun dialogConfirmat(p: Reserva){
        val dialeg = Dialog(this)
        resConf = p
        Log.d("resConf", resConf.toString())
        dialeg.setContentView(R.layout.dialeg_reserva_confirmat)
        dialeg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //dialeg.window?.setWindowAnimations(R.style.animation)
        dialeg.setCancelable(false)

        val llista = mutableListOf<Reserva>()
        llista.add(p)
        dialeg.findViewById<MaterialButton>(R.id.btnConfirmarDRC).setOnClickListener {
            cmprovarArribadaDesti()
            //handler.post(arrivedCheckRunnable!!)
            //arrivedCheckRunnable?.let { handler.post(it) }
            dialeg.dismiss()
        }

        dialeg.show()
    }

    private fun cmprovarArribadaDesti() {
        val crud = CrudApiEasyDrive()
        val crudGeo = CrudGeo(this)

        arrivedCheckRunnable = object : Runnable {
            override fun run() {
                lifecycleScope.launch(Dispatchers.IO) {
                    val reserva = crud.getResevraById(resConf?.id.toString())
                    Log.d("reserva", reserva.toString())
                    if (reserva?.idEstat == 3) {

                        val ubi = crudGeo.getLocationByName(reserva.desti.toString())
                        clientUbi = LatLng(ubi.first().lat, ubi.first().lon)
                        Log.d("clientUbi", clientUbi.toString())
                        withContext(Dispatchers.Main) {
                            // Accede al Fragment y llama a la función
                            val fragment = supportFragmentManager.findFragmentById(R.id.fcv) as? HomeUsuari
                            fragment?.let {
                                it.map?.let { googleMap ->
                                    googleMap.addMarker(
                                        MarkerOptions()
                                            .position(clientUbi!!)
                                            .title("Simulació Ubi destí")
                                            .icon(BitmapDescriptorFactory.fromBitmap(returnBitmap()))
                                    )
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(clientUbi!!, 15f))
                                }
                            }
                            val intent = Intent(this@IniciUsuari, Valoracio::class.java)
                            intent.putExtra("reserva", reserva)
                            startActivity(intent)

                        }

                    }else {
                        // Repetir comprobación en 10 segundos si aún no está en estado 3
                        handler.postDelayed(arrivedCheckRunnable!!, 10000)
                    }
                }
            }

        }

        // Iniciar la primera comprobación
        handler.post(arrivedCheckRunnable!!)
    }




    fun returnBitmap(): Bitmap{
        return getBitmapFromVectorDrawable(R.drawable.baseline_boy_24)
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
        try {
        Glide.with(this)
            .load("http://172.16.24.115:7126/Photos/${user?.fotoPerfil}")
            .error(R.drawable.logo_easydrive)
            .into(binding.btnPerfil)
        }catch (e: Exception){
            binding.btnPerfil.setImageResource(R.drawable.logo_easydrive)
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
                val fragment = supportFragmentManager.findFragmentById(R.id.fcv)
                if (fragment is HomeUsuari) {
                    fragment.carregarUbicacio()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()

        editarHeader()
        afegirFoto()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
    }

}


