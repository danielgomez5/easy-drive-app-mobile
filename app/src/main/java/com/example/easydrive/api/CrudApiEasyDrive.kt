package com.example.easydrive.api

import android.content.Context
import android.util.Log
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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    /*fun getSafeOkHttpClient(): OkHttpClient {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val inputStream = context.resources.openRawResource(R.raw.certificat) // Nombre del archivo sin extensión
        var certificate: java.security.cert.Certificate? = null
        // Intentamos cargar el certificado y manejar el error si ocurre
        try {
            certificate = inputStream.use { certificateFactory.generateCertificate(it) }
            // Si no hay excepciones hasta aquí, el certificado está bien cargado.
        } catch (e: Exception) {
            Log.e("Certificado", "Error al cargar el certificado", e)
            // Puedes manejar el error aquí (por ejemplo, lanzar una excepción personalizada, mostrar un mensaje al usuario, etc.)
            throw RuntimeException("Error al cargar el certificado", e)
        }

        // El código original sigue aquí después de verificar que el certificado se haya cargado correctamente
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("ca", certificate)
        }

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(keyStore)
        }

        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustManagerFactory.trustManagers, SecureRandom())
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManagerFactory.trustManagers[0] as X509TrustManager)
            .build()
    }*/


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

    fun getZona(): List<Zona> ?{
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
    }
}