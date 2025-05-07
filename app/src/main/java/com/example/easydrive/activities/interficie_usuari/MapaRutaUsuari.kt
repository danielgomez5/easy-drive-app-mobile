package com.example.easydrive.activities.interficie_usuari

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easydrive.R
import com.example.easydrive.adaptadors.AdaptadorRVDestins
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.api.geoapify.CrudGeo
import com.example.easydrive.api.openroute.CrudOpenRoute
import com.example.easydrive.dades.DadesPagament
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.dataViatge
import com.example.easydrive.dades.horaViatge
import com.example.easydrive.dades.rutaDesti
import com.example.easydrive.dades.rutaEscollida
import com.example.easydrive.dades.rutaOrigen
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityMapaRutaUsuariBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
    private var markerOrigen: Marker? = null
    private var markerDesti: Marker? = null


    var pagament: DadesPagament?=null
    var preuTotal: Double?=null


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
        var crud = CrudApiEasyDrive()
        afegirText()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        layout_bottom_sheet = findViewById(R.id.layout_bottom_sheet)
        bottom_behavior = BottomSheetBehavior.from(layout_bottom_sheet)

        binding.btnConfirmar.setOnClickListener {
            bottom_behavior.state = BottomSheetBehavior.STATE_EXPANDED
            var dadespagament = crud.getDadesPagament(user?.dni!!)
            if (dadespagament != null){
                layout_bottom_sheet.findViewById<TextInputEditText>(R.id.tiet_numTarjeta)
                    .setText(dadespagament.numeroTarjeta.toString())

                layout_bottom_sheet.findViewById<TextInputEditText>(R.id.tiet_Caducitat)
                    .setText(dadespagament.dataExpiracio.toString())

            }
        }

        binding.tieOrigenMapausuari.setOnClickListener {
            dialogEditarRuta(true)
        }
        binding.tieDestiMapausuari.setOnClickListener {
            dialogEditarRuta(false)
        }

        val tietCaducitat = layout_bottom_sheet.findViewById<TextInputEditText>(R.id.tiet_Caducitat)

        tietCaducitat.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                s?.let {
                    val digits = it.toString().replace("[^\\d]".toRegex(), "")
                    val builder = StringBuilder()

                    // Validar mes
                    if (digits.length >= 2) {
                        val month = digits.substring(0, 2).toIntOrNull()
                        if (month == null || month !in 1..12) {
                            tietCaducitat.setText("")
                            isEditing = false
                            return
                        }
                        builder.append(String.format("%02d", month)).append("/")
                    } else {
                        builder.append(digits)
                        isEditing = false
                        return
                    }

                    // Validar año solo si hay 2 dígitos
                    if (digits.length >= 4) {
                        val yearPart = digits.substring(2, 4)
                        val year = yearPart.toIntOrNull()
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR) % 100

                        if (year != null && year < currentYear) {
                            tietCaducitat.setText("")
                            isEditing = false
                            return
                        }

                        builder.append(yearPart)
                    } else if (digits.length > 2) {
                        builder.append(digits.substring(2)) // Año incompleto, no validar todavía
                    }

                    tietCaducitat.setText(builder.toString())
                    tietCaducitat.setSelection(builder.length.coerceAtMost(tietCaducitat.text?.length ?: 0))
                }

                isEditing = false
            }
        })
    }

    private fun afegirText() {
        poly?.remove()
        binding.tieDestiMapausuari.setText(rutaDesti?.address_line1 + ", " + rutaDesti?.city)
        binding.tieOrigenMapausuari.setText(rutaOrigen?.address_line1+ ", " +rutaOrigen?.city )
    }

    private fun dialogEditarRuta(idHint: Boolean) {
        val dialeg = Dialog(this)
        dialeg.setContentView(R.layout.dialog_escollirrutas)
        dialeg.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialeg.setCancelable(false)

        // Cambiar el hint según el idHint
        if (idHint) {
            dialeg.findViewById<TextInputLayout>(R.id.tilDialog).setHint("Ubicació d'origen")
        } else {
            dialeg.findViewById<TextInputLayout>(R.id.tilDialog).setHint("Ubicació de destí")
        }

        val rcv = dialeg.findViewById<RecyclerView>(R.id.rcvDialog)
        val frameLayout = dialeg.findViewById<FrameLayout>(R.id.frameLayoutResults)
        val emptyState = dialeg.findViewById<LinearLayout>(R.id.ll_emptyState)

        rcv.visibility = View.GONE
        emptyState.visibility = View.GONE

        dialeg.findViewById<AppCompatImageButton>(R.id.buscaDestiD).setOnClickListener {
            val textBuscar = dialeg.findViewById<TextInputEditText>(R.id.tie_rutas).text.toString()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(dialeg.currentFocus?.windowToken, 0)

            if (textBuscar.isNotBlank()) {
                val consulta = textBuscar.replace(" ", "+")
                val crudGeo = CrudGeo(this)
                val listaCarrers = crudGeo.getLocationByName(consulta)

                if (listaCarrers.isNotEmpty()) {
                    val rcv = dialeg.findViewById<RecyclerView>(R.id.rcvDialog)
                    val emptyState = dialeg.findViewById<LinearLayout>(R.id.ll_emptyState)
                    rcv.adapter = AdaptadorRVDestins(listaCarrers)
                    rcv.layoutManager = LinearLayoutManager(this)
                    rcv.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE

                    val constraintSet = ConstraintSet()
                    val constraintLayout = dialeg.findViewById<ConstraintLayout>(R.id.dialogCL)
                    constraintSet.clone(constraintLayout)

                    constraintSet.connect(
                        R.id.frameLayoutResults,
                        ConstraintSet.TOP,
                        R.id.tilDialog,
                        ConstraintSet.BOTTOM
                    )
                    constraintSet.connect(
                        R.id.frameLayoutResults,
                        ConstraintSet.BOTTOM,
                        R.id.btncancelarD,
                        ConstraintSet.TOP
                    )

                    constraintSet.applyTo(constraintLayout)

                    val rvParams = rcv.layoutParams
                    rvParams.height = (450 * resources.displayMetrics.density).toInt()
                    rcv.layoutParams = rvParams

                    dialeg.window?.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                } else {
                    rcv.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                    rutaEscollida = null
                }
            }
        }





        dialeg.findViewById<Button>(R.id.btnaceptarD).setOnClickListener {
            if (rutaEscollida!=null){
                if (idHint) {
                    rutaOrigen = rutaEscollida
                } else {
                    rutaDesti = rutaEscollida
                }
            }else{
                val snack = Snackbar.make(binding.main, "Cap canvi detectat...", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(resources.getColor(R.color.md_theme_secondary, null))
                    .setTextColor(resources.getColor(R.color.md_theme_onSecondary, null))
                    .show()
            }
            afegirText()
            markerDesti?.remove()
            markerOrigen?.remove()
            ubicacioActual = LatLng(rutaOrigen?.lat?.toDouble()!!, rutaOrigen?.lon?.toDouble()!!)
            val ubiacioDesti = LatLng(rutaDesti?.lat?.toDouble()!!, rutaDesti?.lon?.toDouble()!!)
            markerOrigen = map!!.addMarker(
                MarkerOptions().position(ubicacioActual!!)
            )
            markerDesti = map!!.addMarker(
                MarkerOptions().position(ubiacioDesti)
            )
            dibuixarRuta(ubicacioActual, ubiacioDesti)
            dialeg.dismiss()
        }

        dialeg.findViewById<Button>(R.id.btncancelarD).setOnClickListener {
            dialeg.dismiss()
        }

        dialeg.show()
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

            val ubiacioDesti: LatLng = LatLng(rutaDesti?.lat?.toDouble()!!, rutaDesti?.lon?.toDouble()!!)

            markerOrigen = map!!.addMarker(
                MarkerOptions().position(ubicacioActual!!)
            )
            markerDesti = map!!.addMarker(
                MarkerOptions().position(ubiacioDesti)
            )
            Log.d("ubicacioActual antes de la funcion", ubicacioActual.toString())
            dibuixarRuta(ubicacioActual, ubiacioDesti)

        }

        layout_bottom_sheet.findViewById<Button>(R.id.pagar).setOnClickListener {
            var crud = CrudApiEasyDrive()

            val sdfBD = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            pagament = DadesPagament(null,null,null,null,null)
            if (layout_bottom_sheet.findViewById<CheckBox>(R.id.guardarTarjeta).isChecked){
                val input = layout_bottom_sheet.findViewById<TextInputEditText>(R.id.tiet_Caducitat).text.toString()

                val parts = input.split("/")
                if (parts.size == 2) {
                    val mes = parts[0].padStart(2, '0')
                    val any = parts[1].padStart(2, '0')

                    val dataFormatejada = "20$any-$mes-01"  // ejemplo: "2026-04-01"
                    pagament?.dataExpiracio = dataFormatejada
                }

                pagament?.numeroTarjeta = layout_bottom_sheet.findViewById<TextInputEditText>(R.id.tiet_numTarjeta).text.toString()
                pagament?.idUsuari = user?.dni.toString()
                pagament?.titular = user?.nom.toString()+" "+user?.cognom.toString()
            }
            Log.d("Pagament", pagament.toString())

            val currentData = Date()
            var reserva = Reserva(null, null,null,null,null,null,null,null,null)
            reserva.preu = preuTotal!!
            reserva.origen = rutaOrigen?.address_line1+ ", " +rutaOrigen?.city
            reserva.desti = rutaDesti?.address_line1+ ", " +rutaDesti?.city
            reserva.dataReserva = sdfBD.format(currentData)
            if (dataViatge == null){
                dataViatge = sdfBD.format(currentData)
            }
            reserva.horaViatge = horaViatge
            reserva.dataViatge = dataViatge
            reserva.idUsuari = user?.dni
            reserva.idEstat = 1
            Log.d("Reserva", reserva.toString())

            if (crud.insertDadesPagament(pagament!!)){
                Log.d("Insert pagament", "s'ha fet")
            }else{
                Log.d("Insert pagament", "NOOO s'ha fet")
            }

            if (crud.insertReserves(reserva)){
                Log.d("Insert reserva", "s'ha fet")
            }else{
                Log.d("Insert reserva", "NOOO s'ha fet")
            }
            startActivity(Intent(this, IniciUsuari::class.java))

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
        var preu = 1.5 //preu per kilometre
        var preuInciServei = 2.5

        var resposta = crud.getRutaCotxe(start, end)
        if (resposta != null) {
            resposta.features.map {
                coordenada = it.geometry.coordinates
                horas = (it.properties.summary.duration.toInt() / 3600)
                minutos = ((it.properties.summary.duration.toInt()-horas!!*3600)/60)
                segundos = it.properties.summary.duration.toInt()-(horas!!*3600+minutos!!*60)
                layout_bottom_sheet.findViewById<TextView>(R.id.temps_ruta).setText("${horas.toString()}:${minutos.toString()}:${segundos.toString()}")
                layout_bottom_sheet.findViewById<TextView>(R.id.duracio_ruta).setText("${dec.format(it.properties.summary.distance/1000)} km")
                preuTotal = ((it.properties.summary.distance/1000) * preu)+preuInciServei
                layout_bottom_sheet.findViewById<TextView>(R.id.preuTotal).setText(dec.format(preuTotal).toString()+" €")
            }
            layout_bottom_sheet.findViewById<TextView>(R.id.carrerOrigenSheet).setText(rutaOrigen?.address_line1+ ", " +rutaOrigen?.city)
            layout_bottom_sheet.findViewById<TextView>(R.id.carrerDestiSheet).setText(rutaDesti?.address_line1+ ", " +rutaDesti?.city )
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