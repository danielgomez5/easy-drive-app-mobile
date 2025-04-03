package com.example.easydrive.api

import com.example.easydrive.dades.Missatge
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.Zona
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("/api/usuari")
    suspend fun insertUser(@Body usuari: Usuari): Response<Missatge>
    /*@Multipart
    @POST("/api/usuari")
    suspend fun insertUser(
        @Part("usuari") usuari: RequestBody,
        @Part f_perfil: MultipartBody.Part?,
        @Part f_tecnica: MultipartBody.Part?
    ): Response<Missatge>*/

    @Multipart
    @PUT("/api/usuari_image/{id}")
    suspend fun updateUserImage(
        @Path("id") id: String, @Part f_perfil: MultipartBody.Part?,
        @Part f_tecnica: MultipartBody.Part?
    ): Response<Missatge>

    @GET("/api/zones")
    suspend fun getZones(): Response<List<Zona>>

    @GET("/api/zones_comunitats")
    suspend fun getComunitats(): Response<List<String>>

    @GET("/api/zones_provincia/{comunitat}")
    suspend fun getZonesXComunitat(@Path("comunitat") comunitat: String): Response<List<Zona>>

    @GET("/api/zones_ciutat/{provincia}")
    suspend fun getZonesXProvincia(@Path("provincia") provincia: String): Response<List<Zona>>

    /*
    * @GET("/api/zones_ciutat/")
    suspend fun getZonesXComunitat(@Query("comunitat") comunitat: String): Response<List<Zona>>
    * */
}