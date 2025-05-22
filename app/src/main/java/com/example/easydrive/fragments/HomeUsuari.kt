package com.example.easydrive.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easydrive.R
import com.example.easydrive.activities.interficie_usuari.MapaRutaUsuari
import com.example.easydrive.activities.interficie_usuari.Valoracio
import com.example.easydrive.adaptadors.AdaptadorRVDestins
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.api.geoapify.CrudGeo
import com.example.easydrive.dades.Globals
import com.example.easydrive.dades.Globals.clientUbi
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.dataViatge
import com.example.easydrive.dades.horaViatge
import com.example.easydrive.dades.reservaConf
import com.example.easydrive.dades.rutaDesti
import com.example.easydrive.dades.rutaEscollida
import com.example.easydrive.dades.rutaOrigen
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.FragmentHomeUsuariBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeUsuari : Fragment(), OnMapReadyCallback {
    private var cercaRealitzada = false
    private lateinit var binding: FragmentHomeUsuariBinding
    var map: GoogleMap? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var reserva: Boolean = false

    var ubicacioActual: LatLng? = null
    var marcador: Marker? = null

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var checkReservesRunnable: Runnable
    private var isRequestInProgress = false
    val novaPendents = mutableListOf<Reserva>()
    var arribadaDesti: Boolean = false

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
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapa1) as? SupportMapFragment
        if (mapFragment == null) {
            val newMapFragment = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction()
                .replace(R.id.mapa1, newMapFragment)
                .commit()
        } else {
            mapFragment.getMapAsync(this)
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.btnBuscar.setOnClickListener {
            if (rutaEscollida!= null){
                rutaDesti = rutaEscollida
                if (reserva){
                    dialogReserva()
                }else{
                    startActivity(Intent(requireContext(), MapaRutaUsuari::class.java))
                }
            }else{
                val snack = Snackbar.make(binding.homeUsuariLayout, "Has d'escollir un destí per continuar!", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(resources.getColor(R.color.md_theme_secondary, null))
                    .setTextColor(resources.getColor(R.color.md_theme_onSecondary, null))
                    .show()
            }
        }
        binding.buscaDesti.setOnClickListener {
            buscaDesti()
        }

        binding.toggleButton.addOnButtonCheckedListener{group, checkedId, isChecked ->
            if (isChecked){
                when(checkedId){
                    R.id.btnDemanaAra ->{
                        reserva = false
                    }
                    R.id.btnReserva ->{
                        reserva = true

                    }
                }
            }
        }
        /*val intervalMillis: Long = 20000 // cada 10 segundos

        checkReservesRunnable = object : Runnable {
            override fun run() {
                comprovarArribada()
                handler.postDelayed(this, intervalMillis)
            }

        }*/


        return binding.root
    }

    private fun dialogReserva() {
        val dialeg = Dialog(requireContext())
        dialeg.setContentView(R.layout.dialog_reserva)
        dialeg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //dialeg.window?.setWindowAnimations(R.style.animation)
        dialeg.setCancelable(false)

        dialeg.findViewById<TextInputEditText>(R.id.tie_dialogdate).setOnClickListener {
            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona la data de reserva")
                .setCalendarConstraints(constraints)
                .build()

            datePicker.show(parentFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val selectedDate = Date(selection)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val sdfBD = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                // Mostrar la fecha en el campo, aunque sea inválida
                dialeg.findViewById<TextInputEditText>(R.id.tie_dialogdate).setText(sdf.format(selectedDate))

                // Guardar la fecha en el objeto (a validar después)
                dataViatge = sdfBD.format(selectedDate)

                // Limpiar posibles errores antiguos
                dialeg.findViewById<TextInputEditText>(R.id.tie_dialogdate).error = null
            }

        }
        dialeg.findViewById<TextInputEditText>(R.id.tie_dialoghora).setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val isSystem24Hour = is24HourFormat(requireContext())
            val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

            val picker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(clockFormat)
                    .setInputMode(INPUT_MODE_KEYBOARD)
                    .setHour(hour)
                    .setMinute(minute)
                    .setTitleText("Selecciona una hora diferent")
                    .build()

            picker.show(parentFragmentManager, "TIME_PICKER")

            picker.addOnPositiveButtonClickListener {
                val hora = picker.hour
                val minuto = picker.minute
                horaViatge = "$hora:$minuto:00"
                dialeg.findViewById<TextInputEditText>(R.id.tie_dialoghora).setText("$hora:$minuto")
            }

        }

        dialeg.findViewById<Button>(R.id.btnaceptarD).setOnClickListener {
            startActivity(Intent(requireContext(), MapaRutaUsuari::class.java))
            dialeg.dismiss()
        }
        dialeg.findViewById<Button>(R.id.btncancelarD).setOnClickListener {
            dialeg.dismiss()
        }

        dialeg.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toggleButton.check(binding.btnDemanaAra.id)
    }

    override fun onMapReady(googleMap: GoogleMap){
        map = googleMap
        carregarUbicacio()
    }

    private fun buscaDesti() {
        ocultarTeclat()
        cercaRealitzada = true
        var textBuscar = binding.tieDestiFragmentHU.text.toString()
        textBuscar = textBuscar.replace(" ", "+")

        binding.progressBar.visibility = View.VISIBLE
        binding.rcv.visibility = View.GONE
        binding.llEmptyState.visibility = View.GONE
        binding.tvResultats.visibility = View.GONE

        lifecycleScope.launch {
            val crudGeo = CrudGeo(requireContext())

            // Ejecutar en segundo plano
            val listaCarrers = withContext(Dispatchers.IO) {
                crudGeo.getLocationByName(textBuscar)
            }

            // Volvemos al hilo principal
            binding.progressBar.visibility = View.GONE

            if (listaCarrers.isNotEmpty()) {
                binding.rcv.visibility = View.VISIBLE
                binding.llEmptyState.visibility = View.GONE
                binding.tvResultats.visibility = View.VISIBLE
                binding.rcv.adapter = AdaptadorRVDestins(listaCarrers)
                binding.rcv.layoutManager = LinearLayoutManager(requireContext())
            } else {
                binding.rcv.visibility = View.GONE
                binding.llEmptyState.visibility = View.VISIBLE
                binding.tvResultats.visibility = View.GONE
                if (cercaRealitzada) {
                    rutaEscollida = null
                    binding.ivEmptyImage.setImageResource(R.drawable.no_results)
                    binding.tvEmptyText.text = "Epa, sembla que no tenim disponibilitat en aquesta zona..."
                } else {
                    binding.ivEmptyImage.setImageResource(R.drawable.car)
                    binding.tvEmptyText.text = "Planeja el teu proper viatge!"
                }
            }

            expandirRecyclerView()
        }
    }



    fun carregarUbicacio() {
        if (!isAdded) return

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Permisos no concedits", Toast.LENGTH_SHORT).show()
            return
        }

        map?.isMyLocationEnabled = false
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (!isAdded) return@addOnSuccessListener

            if (location != null) {
                clientUbi = LatLng(location.latitude, location.longitude)
                marcador = map?.addMarker(
                    MarkerOptions()
                        .position(clientUbi!!)
                        .title("Simulació Ubi actual")
                        .icon(BitmapDescriptorFactory.fromBitmap(returnBitmap()))
                )
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(clientUbi!!, 15f),2000,null)
                rutaOrigen = CrudGeo(requireContext()).getLocationByLatLon(
                    clientUbi?.latitude.toString(),
                    clientUbi?.longitude.toString()
                )
            } else {
                Toast.makeText(requireContext(), "Ubicació no disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun returnBitmap(): Bitmap{
        return getBitmapFromVectorDrawable(R.drawable.baseline_boy_24)
    }

    fun getBitmapFromVectorDrawable(@DrawableRes drawableId: Int): Bitmap {
        val context = context ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
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


    private fun ocultarTeclat() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.tieDestiFragmentHU.windowToken, 0)
    }

    private fun expandirRecyclerView() {
        val constraintSet = ConstraintSet()
        val constraintLayout = binding.root as ConstraintLayout
        constraintSet.clone(constraintLayout)

        // Reduïm l'altura del mapa
        val heightInPx = (100 * resources.displayMetrics.density).toInt()
        constraintSet.constrainHeight(R.id.mapa, heightInPx)

        // Posem les constraints de la card sota el mapa redimensionat
        constraintSet.connect(
            R.id.cardHomeUsuari,
            ConstraintSet.TOP,
            R.id.mapa,
            ConstraintSet.BOTTOM
        )

        constraintSet.connect(
            R.id.cardHomeUsuari,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM
        )

        constraintSet.applyTo(constraintLayout)

        // Augmentem l'altura del rv amb els carrers
        val params = binding.rcv.layoutParams
        params.height = (300 * resources.displayMetrics.density).toInt()
        binding.rcv.layoutParams = params
    }


}