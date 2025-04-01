package com.example.easydrive.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.easydrive.R
import com.example.easydrive.dades.Missatge
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.Zona
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.X509TrustManager
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import kotlin.coroutines.CoroutineContext

class CrudApiEasyDrive() : CoroutineScope {
    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val urlBase = "http://172.16.24.115:7126/"

    private fun getClient(): OkHttpClient {
        var logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder().addInterceptor(logging).build()
    }

    private fun getRetrofit(): Retrofit {
        val gson = GsonBuilder().setLenient().create()
        return Retrofit.Builder().baseUrl(urlBase).client(getClient())
            .addConverterFactory(GsonConverterFactory.create(gson)).build()


    }

    fun insertUsuari(usuari: Usuari): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).insertUser(usuari)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

    /*fun getZona(): List<Zona> ?{
        var resposta : Response<List<Zona>>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getZones()
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null
    }*/

    fun getComunitats(): List<String>? {
        var resposta : Response<List<String>>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getComunitats()
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null
    }

    fun getZonaxComunitat(comunitat :String): List<Zona>? {
        var resposta : Response<List<Zona>>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getZonesXComunitat(comunitat)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null
    }
}

    /*fun pujaArxiu(ruta: String, context:Context): Boolean{
        val file = File(ruta)
        if (!file.exists()){
            Toast.makeText(context, "No existeix l'arxiu", Toast.LENGTH_LONG).show()
            Log.i("pujaArxiu", "No existeix l'arxiu: "+ruta)
            return false
        }else {
            val foto : RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val bodyImatge = MultipartBody.Part.createFormData("image", file.name, foto)

            var resposta : Response<Missatge>? = null
            runBlocking {
                val cor = launch {
                    resposta = getRetrofit().create(ApiService::class.java).uploadFile(bodyImatge)
                }
                cor.join()
            }
            if (resposta!!.isSuccessful)
                return true
            else{
                Log.i("resposta", resposta!!.message().toString())
                return false
            }

        }

    }*/