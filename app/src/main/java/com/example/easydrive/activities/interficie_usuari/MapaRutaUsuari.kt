package com.example.easydrive.activities.interficie_usuari

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easydrive.R
import com.example.easydrive.api.openroute.CrudOpenRoute
import com.example.easydrive.dades.rutaEscollida
import com.example.easydrive.databinding.ActivityMapaRutaUsuariBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.text.DecimalFormat
import kotlin.div
import kotlin.text.toInt

class MapaRutaUsuari : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapaRutaUsuariBinding
    private lateinit var layout_bottom_sheet: ConstraintLayout
    private lateinit var bottom_behavior: BottomSheetBehavior<ConstraintLayout>

    var poly: Polyline? = null
    var map: GoogleMap? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var ubicacioActual: LatLng? = null

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
        poly?.remove()
        binding.tieDestiMapausuari.setText(rutaEscollida?.address_line1 + ", " + rutaEscollida?.city)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        layout_bottom_sheet = findViewById(R.id.layout_bottom_sheet)
        bottom_behavior = BottomSheetBehavior.from(layout_bottom_sheet)

        binding.btnConfirmar.setOnClickListener {
            bottom_behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        map?.isMyLocationEnabled = true // Este muestra el icono azul de ubicación
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location ->
            ubicacioActual = LatLng(location.latitude, location.longitude)
            Log.d("ubicacioActual con location", ubicacioActual.toString())
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(ubicacioActual!!, 15f),
                2000,
                null
            )
            val ubiacioDesti: LatLng = LatLng(rutaEscollida?.lat?.toDouble()!!, rutaEscollida?.lon?.toDouble()!!)

            map!!.addMarker(
                MarkerOptions().position(ubiacioDesti)
            )
            Log.d("ubicacioActual antes de la funcion", ubicacioActual.toString())
            dibuixarRuta(ubicacioActual, ubiacioDesti)

        }


    }

    private fun dibuixarRuta(ubicacioActual: LatLng?, ubicacioDesti: LatLng) {
        val crud = CrudOpenRoute(this)

        val start = ubicacioActual?.longitude.toString() + "," + ubicacioActual?.latitude.toString()
        val end = ubicacioDesti?.longitude.toString() + "," + ubicacioDesti?.latitude.toString()

        Log.d("start", start.toString())
        Log.d("end", end.toString())

        var coordenada: List<List<Double>>? = null
        var horas: Int? = null
        var minutos: Int? = null
        var segundos: Int? = null
        val dec = DecimalFormat("#,###.00")

        var resposta = crud.getRutaCotxe(start, end)
        if (resposta != null) {
            resposta.features.map {
                coordenada = it.geometry.coordinates
                horas = (it.properties.summary.duration.toInt() / 3600)
                minutos = ((it.properties.summary.duration.toInt()-horas!!*3600)/60)
                segundos = it.properties.summary.duration.toInt()-(horas!!*3600+minutos!!*60)
                layout_bottom_sheet.findViewById<TextView>(R.id.temps_ruta).setText("${horas.toString()}:${minutos.toString()}:${segundos.toString()}")
                layout_bottom_sheet.findViewById<TextView>(R.id.duracio_ruta).setText("${dec.format(it.properties.summary.distance/1000)} km")
            }
            drawRoute(map!!, coordenada!!)
        } else {
            Log.d("resposta api", resposta.toString())
            Toast.makeText(this, "No hi ha resposta", Toast.LENGTH_LONG)
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

}