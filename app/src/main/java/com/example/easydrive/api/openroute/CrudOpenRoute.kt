package com.example.easydrive.api.openroute

import android.content.Context
import android.util.Log
import com.example.easydrive.R
import com.example.easydrive.dades.OpenRouteDades
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
import java.net.SocketTimeoutException
import kotlin.coroutines.CoroutineContext

class CrudOpenRoute (val context: Context) : CoroutineScope {
    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val urlBase = "https://api.openrouteservice.org/"
    val api = context.resources.getString(R.string.api_openroute)

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

    fun getRutaCotxe(start: String, end: String): OpenRouteDades? {
        var resposta: Response<OpenRouteDades>? = null
        var resultat: OpenRouteDades? = null
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                resposta = getRetrofit()
                    .create(ApiServiceOpenRoute::class.java)
                    .getRutaEnCoche(api, start, end)

                if (resposta!!.isSuccessful) {
                    resultat = resposta!!.body()
                } else {
                    Log.e("API_ERROR", "Codi error: ${resposta!!.code()}, missatge: ${resposta!!.message()}")
                }
            } catch (e: SocketTimeoutException) {
                Log.e("API_TIMEOUT", "S'ha produït un timeout")
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Error inesperat: ${e.message}")
            }
        }

        runBlocking {
            job.join() // Esperem que acabi la corrutina
        }

        return resultat
    }


}