package com.example.easydrive.api.esaydrive

import android.util.Log
import com.example.easydrive.dades.ChangePasswordRequest
import com.example.easydrive.dades.Cotxe
import com.example.easydrive.dades.DadesPagament
import com.example.easydrive.dades.LoginRequest
import com.example.easydrive.dades.Missatge
import com.example.easydrive.dades.Reserva
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.UsuariCotxeDTO
import com.example.easydrive.dades.Viatja
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
import okhttp3.RequestBody.Companion.asRequestBody
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

    fun insertRelacioCotxeUsuari(relacioUsuariCotxe: UsuariCotxeDTO?): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).assignarCotxeAUsuari(relacioUsuariCotxe)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

    fun insertDadesPagament(pagament: DadesPagament): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).afegirPagament(pagament)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

    fun insertReserves(reserva: Reserva): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).afegirReserva(reserva)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

    fun insertViatge(viatge: Viatja): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).afegirViatge(viatge)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }


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

    /*fun getUsuariXCorreuContra(email: String, password: String): Usuari?{
        var resposta: Response<Usuari>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getUsuariViaContraPass(email, password)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null
    }*/
    fun loginUsuari(email: String, password: String): Usuari? {
        var resposta: Response<Usuari>? = null
        runBlocking {
            val job = launch {
                val loginData = LoginRequest(email, password)
                resposta = getRetrofit().create(ApiService::class.java).loginUsuari(loginData)
            }
            job.join()
        }

        return if (resposta?.isSuccessful == true) {
            resposta!!.body()
        } else {
            null
        }
    }

    fun getDadesPagament(idUser: String): DadesPagament?{
        var resposta: Response<DadesPagament>? = null
        runBlocking {
            val job = launch {
                resposta = getRetrofit().create(ApiService::class.java).getDadesPagamentByUsuari(idUser)
            }
            job.join()
        }

        return if (resposta?.isSuccessful == true) {
            resposta!!.body()
        } else {
            null
        }
    }

    fun getDispoTaxista(id: String): Boolean{
        var resposta: Response<Boolean>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getDispoTaxista(id)
            }
            cor.join()
        }
        return resposta?.body() == true
    }

    fun getReservesByUsuari(id:String): MutableList<Reserva>?{
        var resposta: Response<MutableList<Reserva>>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getReservesByUsuari(id)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null
    }

    fun getReservesPendents(): MutableList<Reserva>?{
        var resposta: Response<MutableList<Reserva>>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getReservesPendents()
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null
    }

    fun getAllViatgesByUsuari(id_usuari:String): List<Viatja>?{
        var resposta: Response<List<Viatja>?>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getAllViatgesByUser(id_usuari)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null

    }

    fun getUsuariById(id: String): Usuari?{
        var resposta: Response<Usuari>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getUserById(id)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null
    }

    fun getResevraById(id: String) :Reserva?{
        var resposta: Response<Reserva>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getReservaById(id)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null
    }

    fun getAllCotxesByUsuari(id: String): List<Cotxe>?{
        var resposta: Response<List<Cotxe>>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getCotxesTaxista(id)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return resposta!!.body()
        else
            return null
    }

    fun getCotxeByMatr(id: String) :Cotxe?{
        var resposta: Response<Cotxe>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).getCotxeByMatricula(id)
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
        val requestBodyPerfil = rutaFitxaTecnica.asRequestBody("application/pdf".toMediaTypeOrNull())

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

    fun updateUsuari(id: String?, usuari: Usuari): Boolean {
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).updateUser(id!!, usuari)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

    fun changePassword(id: String?, request: ChangePasswordRequest): Boolean {
        var resposta: Response<String>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).changePassword(id, request)
                resposta = getRetrofit().create(ApiService::class.java).changePassword(id, request)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

    fun updateDispoTaxista(id: String, dispo: Boolean): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).updateDisponiblitat(id,dispo)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

    fun changeEstatReserva(id: String, reserva: Reserva): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).updateReserva(id,reserva)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

    fun updateViatge(id:String, viatge: Viatja): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).updateViatge(id,viatge)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

    fun updateCotxe(id:String, cotxe: Cotxe): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).updateCotxe(id,cotxe)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }


    // Dels
    fun delUser(id: String): Boolean {
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).delUser(id)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

    fun delReserva(id: String): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).delReserva(id)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }

    fun delCotxe(id: String): Boolean{
        var resposta: Response<Missatge>? = null
        runBlocking {
            val cor = launch {
                resposta = getRetrofit().create(ApiService::class.java).delCotxe(id)
            }
            cor.join()
        }
        if (resposta!!.isSuccessful)
            return true
        else
            return false
    }
}