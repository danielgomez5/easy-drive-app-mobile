package com.example.easydrive.api.geoapify

import android.content.Context
import com.example.easydrive.R
import com.example.easydrive.api.esaydrive.ApiService
import com.example.easydrive.dades.GeoapifyDades
import com.example.easydrive.dades.GeoapifyResponse
import com.example.easydrive.dades.LoginRequest
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.CoroutineContext

class CrudGeo (val context: Context) : CoroutineScope {
    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val urlBase = "https://api.geoapify.com/"
    var apiKey = context.resources.getString(R.string.api_geoapify)

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

    fun getLocationByName(nomCarrer: String): List<GeoapifyDades> {
        var resposta: Response<GeoapifyResponse>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiServiceGeo::class.java).buscarDesti(nomCarrer, apiKey)
            }
            cor.join()
        }

        return if (resposta!!.isSuccessful) {
            resposta!!.body()?.results!!
        } else {
            emptyList()
        }
    }

    fun getLocationByLatLon(latitut: String, longitud: String): List<GeoapifyDades> {
        var resposta: Response<GeoapifyResponse>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiServiceGeo::class.java).buscarDestiLatLon(latitut,longitud, apiKey)
            }
            cor.join()
        }

        return if (resposta!!.isSuccessful) {
            resposta!!.body()?.results!!
        } else {
            emptyList()
        }
    }

}