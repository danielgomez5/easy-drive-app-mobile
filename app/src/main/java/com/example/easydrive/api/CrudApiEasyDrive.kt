package com.example.easydrive.api


import android.util.Log
import com.example.easydrive.dades.Cotxe
import com.example.easydrive.dades.Missatge
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.Zona
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
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

    //Insert
    fun insertUsuari(usuari: Usuari): Boolean {
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

    fun insertUsuari2(usuari: Usuari): Boolean {
        val gson = Gson()  // Crear la instancia de Gson

        // Archivos para las imágenes
        val filePerfil = if (!usuari.fotoPerfil.isNullOrEmpty()) File(usuari.fotoPerfil) else null
        val fileTecnica = if (!usuari.fotoCarnet.isNullOrEmpty()) File(usuari.fotoCarnet) else null

        var partPerfil: MultipartBody.Part? = null
        if (filePerfil != null) {
            val requestBodyPerfil = filePerfil.asRequestBody("image/*".toMediaTypeOrNull())
            partPerfil = MultipartBody.Part.createFormData("fotoPerfil", filePerfil.name, requestBodyPerfil)
        }

        var partCarnet: MultipartBody.Part? = null
        if (fileTecnica != null) {
            val requestBodyTecnica = fileTecnica.asRequestBody("image/*".toMediaTypeOrNull())
            partCarnet = MultipartBody.Part.createFormData("fotoCarnet", fileTecnica.name, requestBodyTecnica)
        }

        // Crear un RequestBody para el objeto Usuari (en formato JSON)
        val usuariJson = gson.toJson(usuari).toRequestBody("application/json".toMediaTypeOrNull())

        // Realizar la llamada al servidor
        var respuesta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                respuesta = getRetrofit().create(ApiService::class.java).insertUser2(usuariJson, partPerfil, partCarnet)
            }
            cor.join()
        }

        if (respuesta!!.isSuccessful) {
            Log.d("InsertUsuari", "Usuario añadido correctamente")
            return true
        } else {
            Log.d("InsertUsuari", "Error en la respuesta: ${respuesta?.message()}")
            return false
        }
    }

    fun insertCotxe(cotxe: Cotxe): Boolean {
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).insertCotxe(cotxe)
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

    //GET
    fun getComunitats(): List<String>? {
        var resposta: Response<List<String>>? = null
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

    fun getZonaxComunitat(comunitat: String): List<String>? {
        var resposta: Response<List<String>>? = null
        runBlocking {
            val cor = launch {
                resposta =
                    getRetrofit().create(ApiService::class.java).getZonesXComunitat(comunitat)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null
    }

    fun getZonaxProvincia(provincia: String): List<Zona>? {
        var resposta: Response<List<Zona>>? = null
        runBlocking {
            val cor = launch {
                resposta =
                    getRetrofit().create(ApiService::class.java).getZonesXProvincia(provincia)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null
    }

    //Update
    fun updateUserFoto(id: String, rutaPerfil: String?): Boolean {
        val filePerfil = File(rutaPerfil)

        val requestBodyPerfil = filePerfil.asRequestBody("image/*".toMediaTypeOrNull())

        val partPerfil = MultipartBody.Part.createFormData("f_perfil", filePerfil.name, requestBodyPerfil)

        return runBlocking {
            try {
                val resposta = getRetrofit().create(ApiService::class.java)
                    .updateUserImage(id, partPerfil)
                if (resposta.isSuccessful) {
                    Log.d("API", "Imagen subida correctamente")
                    true
                } else {
                    Log.e("APIError", "Error en la respuesta: ${resposta.message()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("APIError", "Error al realizar la petición: ${e.localizedMessage}")
                false
            }
        }
    }

    fun updateUserFotoPerfilCarnet(id: String, rutaPerfil: String?, fotoCarnet: String?): Boolean {
        val filePerfil = File(rutaPerfil)
        val fileTecnica = File(fotoCarnet)

        val requestBodyPerfil = filePerfil.asRequestBody("image/*".toMediaTypeOrNull())
        val requestBodyTecnica = fileTecnica.asRequestBody("image/*".toMediaTypeOrNull())

        val partPerfil =
            MultipartBody.Part.createFormData("f_perfil", filePerfil.name, requestBodyPerfil)
        val partTecnica = MultipartBody.Part.createFormData("f_tecnica", fileTecnica.name, requestBodyTecnica)

        return runBlocking {
            try {
                val resposta = getRetrofit().create(ApiService::class.java)
                    .updateUserImageCarnet(id, partPerfil, partTecnica)
                if (resposta.isSuccessful) {
                    Log.d("API", "Imagen subida correctamente")
                    true
                } else {
                    Log.e("APIError", "Error en la respuesta: ${resposta.message()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("APIError", "Error al realizar la petición: ${e.localizedMessage}")
                false
            }
        }
    }

    fun updateCotxeFitxaTecnica(id: String, rutaFitxa: String): Boolean {
        val rutaFitxaTecnica = File(rutaFitxa)
        val requestBodyPerfil = rutaFitxaTecnica.asRequestBody("file/*".toMediaTypeOrNull())

        val fitxaTecnica =
            MultipartBody.Part.createFormData("f_tecnic", rutaFitxaTecnica.name, requestBodyPerfil)

        return runBlocking {
            try {
                val resposta = getRetrofit().create(ApiService::class.java)
                    .updateCotxeFTecnic(id, fitxaTecnica)
                if (resposta.isSuccessful) {
                    Log.d("API", "Archivo subido correctamente")
                    true
                } else {
                    Log.e("APIError", "Error en la respuesta: ${resposta.message()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("APIError", "Error al realizar la petición: ${e.localizedMessage}")
                false
            }
        }
    }

    fun updateZonaCoberta(id: String): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).updateZonaCuberta(id)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

}