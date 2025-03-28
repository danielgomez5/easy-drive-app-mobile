package com.example.easydrive.activities.interficie_usuari

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.easydrive.R
import com.example.easydrive.databinding.ActivityIniciUsuariBinding
import com.example.easydrive.fragments.HomeUsuari

class IniciUsuari : AppCompatActivity() {
    lateinit var binding: ActivityIniciUsuariBinding
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
        binding.bnv.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.menuInici -> canviaFragment(HomeUsuari())
                else -> canviaFragment(HomeUsuari())
            }
        }
    }

    fun canviaFragment(f : Fragment) : Boolean{
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fcv,f)
        transaction.commit()
        return true
    }
}