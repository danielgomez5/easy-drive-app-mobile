package com.example.easydrive.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.easydrive.R
import com.example.easydrive.activities.interficie_usuari.MapaRutaUsuari
import com.example.easydrive.databinding.FragmentHomeUsuariBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class HomeUsuari : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener {
    private lateinit var binding: FragmentHomeUsuariBinding
    var map: GoogleMap? = null

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
        binding.btnBuscar.setOnClickListener {
            startActivity(Intent(requireContext(), MapaRutaUsuari::class.java))
        }
        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap){
        map = googleMap
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
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
        Toast.makeText(requireContext(), "Ubicación actual: ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(requireContext(), "Botón de ubicación pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

}