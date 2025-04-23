package com.example.easydrive.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easydrive.R
import com.example.easydrive.activities.interficie_usuari.MapaRutaUsuari
import com.example.easydrive.adaptadors.AdaptadorRVDestins
import com.example.easydrive.databinding.FragmentHomeUsuariBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeUsuari : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentHomeUsuariBinding
    private var map: GoogleMap? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var desti: Geocoder?=null
    private var adreces = mutableListOf<Address>()
    private var noms = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeUsuariBinding.inflate(inflater,container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapa) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.btnBuscar.setOnClickListener {
            startActivity(Intent(requireContext(), MapaRutaUsuari::class.java))
        }
        desti = Geocoder(requireContext())
        binding.buscaDesti.setOnClickListener {
            buscaDesti()
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap){
        map = googleMap
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Permisos de ubicación no concedidos", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "No s'ha pogut obtenir la ubicació", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscaDesti() {
        val textBuscar = binding.tieDestiFragmentHU.text.toString()

        if (textBuscar.isNotBlank()) {
            val resultats = desti?.getFromLocationName(textBuscar, 5)
            Log.d("resultados", resultats.toString())
            adreces.clear()

            if (!resultats.isNullOrEmpty()) {
                for (adreca in resultats) {
                    adreces.add(adreca)//
                }
            } else {
                Toast.makeText(requireContext(), "No s'ha trobat cap destinació amb aquest nom.", Toast.LENGTH_SHORT).show()
            }
            binding.rcv.layoutManager = LinearLayoutManager(requireContext())
            binding.rcv.adapter = AdaptadorRVDestins(adreces)
        } else {
            Toast.makeText(requireContext(), "Introdueix un destí per buscar", Toast.LENGTH_SHORT).show()
        }
    }



}