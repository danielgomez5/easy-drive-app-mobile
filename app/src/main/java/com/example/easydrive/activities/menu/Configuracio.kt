package com.example.easydrive.activities.menu

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.easydrive.R
import com.example.easydrive.adaptadors.IdiomaAdapter
import com.example.easydrive.api.esaydrive.CrudApiEasyDrive
import com.example.easydrive.dades.DadesPagament
import com.example.easydrive.dades.listIdiomes
import com.example.easydrive.dades.user
import com.example.easydrive.databinding.ActivityConfiguracioBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar

class Configuracio : AppCompatActivity() {
    lateinit var binding : ActivityConfiguracioBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityConfiguracioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val crud = CrudApiEasyDrive()

        var dadesPagament = crud.getDadesPagament(user?.dni!!)

        actualitzarVistaPagament(dadesPagament)



        binding.btnModifyPayment.setOnClickListener {
            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_modificar_dades_pagament, null)
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(bottomSheetView)

            val etNumTarjeta = bottomSheetView.findViewById<TextInputEditText>(R.id.etNumTarjeta)
            val etCaducitat = bottomSheetView.findViewById<TextInputEditText>(R.id.etCaducitat)
            val btnGuardar = bottomSheetView.findViewById<Button>(R.id.btnGuardar)

            dadesPagament?.let {
                etNumTarjeta.setText(it.numeroTarjeta)
                etCaducitat.setText(formatSqlDateToMMYY(it.dataExpiracio))
            }

            btnGuardar.setOnClickListener {
                val numero = etNumTarjeta.text.toString()
                val caducitat = etCaducitat.text.toString()

                var isValid = true

                if (numero.length < 12) {
                    etNumTarjeta.error = "Número no vàlid"
                    isValid = false
                }

                val parts = caducitat.split("/")
                if (parts.size != 2 || parts[0].toIntOrNull() !in 1..12 || parts[1].length != 2) {
                    etCaducitat.error = "Format ha de ser MM/AA"
                    isValid = false
                }

                if (!isValid) return@setOnClickListener

                dadesPagament?.apply {
                    this.numeroTarjeta = numero
                    this.dataExpiracio = convertirCaducitatASQLDate(caducitat)

                }

                lifecycleScope.launch(Dispatchers.IO) {
                    crud.updateDadesPagament(dadesPagament?.id!!, dadesPagament!!)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Configuracio, "Dades actualitzades", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()

                        binding.tvCardNumber.text = "**** **** **** ${numero.takeLast(4)}"
                        binding.tvCvv.text = "CVV: ***"
                        binding.tvExpiryDate.text = "Caducitat: $caducitat"
                    }
                }
            }

            etCaducitat.addTextChangedListener(object : TextWatcher {
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
                                etCaducitat.setText("")
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
                                etCaducitat.setText("")
                                isEditing = false
                                return
                            }

                            builder.append(yearPart)
                        } else if (digits.length > 2) {
                            builder.append(digits.substring(2)) // Año incompleto, no validar todavía
                        }

                        etCaducitat.setText(builder.toString())
                        etCaducitat.setSelection(builder.length.coerceAtMost(etCaducitat.text?.length ?: 0))
                    }

                    isEditing = false
                }
            })

            dialog.show()
        }

        binding.btnDeletePayment.setOnClickListener {
            if (dadesPagament != null) {
                AlertDialog.Builder(this)
                    .setTitle("Confirmació")
                    .setMessage("Segur que vols eliminar les dades de pagament?")
                    .setPositiveButton("Eliminar") { dialog, _ ->
                        if (crud.delDadesPagament(dadesPagament?.id!!)) {
                            Toast.makeText(this, "Dades eliminades correctament", Toast.LENGTH_SHORT).show()
                            dadesPagament = null
                            actualitzarVistaPagament(dadesPagament)
                        } else {
                            Toast.makeText(this, "Error eliminant dades", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel·lar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }

        binding.btnAddPayment.setOnClickListener {
            var dp: DadesPagament? = null
            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_modificar_dades_pagament, null)
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(bottomSheetView)

            val etNumTarjeta = bottomSheetView.findViewById<TextInputEditText>(R.id.etNumTarjeta)
            val etCaducitat = bottomSheetView.findViewById<TextInputEditText>(R.id.etCaducitat)
            val btnGuardar = bottomSheetView.findViewById<Button>(R.id.btnGuardar)

            btnGuardar.setOnClickListener {
                val numero = etNumTarjeta.text.toString()
                val caducitat = etCaducitat.text.toString()

                var isValid = true

                if (numero.length < 12) {
                    etNumTarjeta.error = "Número no vàlid"
                    isValid = false
                }

                val parts = caducitat.split("/")
                if (parts.size != 2 || parts[0].toIntOrNull() !in 1..12 || parts[1].length != 2) {
                    etCaducitat.error = "Format ha de ser MM/AA"
                    isValid = false
                }

                if (!isValid) return@setOnClickListener

                dp = DadesPagament(null, numero, "${user?.nom} ${user?.cognom}", convertirCaducitatASQLDate(caducitat), user?.dni)

                lifecycleScope.launch(Dispatchers.IO) {{

                }
                    if (crud.insertDadesPagament(dp!!)){
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@Configuracio, "Dades de pagament afegides", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()

                            actualitzarVistaPagament(dp)
                        }
                    }else{
                        Toast.makeText(this@Configuracio, "Hi ha hagut un problema afegint les dades", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            }

            etCaducitat.addTextChangedListener(object : TextWatcher {
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
                                etCaducitat.setText("")
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
                                etCaducitat.setText("")
                                isEditing = false
                                return
                            }

                            builder.append(yearPart)
                        } else if (digits.length > 2) {
                            builder.append(digits.substring(2))
                        }

                        etCaducitat.setText(builder.toString())
                        etCaducitat.setSelection(builder.length.coerceAtMost(etCaducitat.text?.length ?: 0))
                    }

                    isEditing = false
                }
            })
            dialog.show()
        }


        binding.imagebtnR1.setOnClickListener {
            finish()
        }



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualitzarVistaPagament(dades: DadesPagament?) {
        val cardPayment = binding.cardPaymentInfo
        val btnModify = binding.btnModifyPayment
        val btnDelete = binding.btnDeletePayment
        val emptyState = binding.llEmptyStatePayment

        if (dades != null) {
            emptyState.visibility = View.GONE
            cardPayment.visibility = View.VISIBLE
            btnModify.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE

            val numeroMostrat = "**** **** **** " + dades.numeroTarjeta?.takeLast(4)
            val caducitatFormatejada = formatCaducitat(dades.dataExpiracio ?: "")
            binding.tvCardNumber.text = numeroMostrat
            binding.tvExpiryDate.text = "Caducitat: $caducitatFormatejada"
            binding.tvCvv.text = "CVV: ***"
        } else {
            emptyState.visibility = View.VISIBLE
            cardPayment.visibility = View.GONE
            btnModify.visibility = View.GONE
            btnDelete.visibility = View.GONE

            binding.tvCardNumber.text = "Sense dades"
            binding.tvExpiryDate.text = ""
            binding.tvCvv.text = ""
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun formatCaducitat(input: String): String {
        return try {
            val inputFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                DateTimeFormatter.ofPattern("[MM/yy][MM-yyyy][yyyy-MM-dd]")
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            val parsed = YearMonth.parse(input, inputFormat)
            parsed.format(DateTimeFormatter.ofPattern("MM/yy"))
        } catch (e: Exception) {
            input
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertirCaducitatASQLDate(input: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("MM/yy")
            val ym = YearMonth.parse(input, formatter)
            ym.atDay(1).toString()
        } catch (e: Exception) {
            ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatSqlDateToMMYY(sqlDate: String?): String {
        return try {
            if (sqlDate.isNullOrEmpty()) return ""
            val parsed = LocalDate.parse(sqlDate)
            val formatter = DateTimeFormatter.ofPattern("MM/yy")
            parsed.format(formatter)
        } catch (e: Exception) {
            sqlDate ?: ""
        }
    }
}