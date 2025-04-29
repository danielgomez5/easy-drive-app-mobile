package com.example.easydrive.api.esaydrive

import com.example.easydrive.dades.Cotxe
import com.example.easydrive.dades.DadesPagament
import com.example.easydrive.dades.LoginRequest
import com.example.easydrive.dades.Missatge
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.UsuariCotxeDTO
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

interface ApiService {

    //Posts
    @POST("/api/usuari")
    suspend fun insertUser(@Body usuari: Usuari): Response<Missatge>

    @POST("/api/cotxe")
    suspend fun insertCotxe(@Body cotxe: Cotxe): Response<Missatge>

    @POST("/api/usuari-cotxe")
    suspend fun assignarCotxeAUsuari(@Body relacio: UsuariCotxeDTO?): Response<Missatge>

    @POST("/api/usuari-pagament")
    suspend fun afegirPagament(@Body pagament: DadesPagament): Response<Missatge>

    /*@Multipart
    @POST("/api/usuari")
    suspend fun insertUser2(
        @Part("usuari") usuari: RequestBody,
        @Part fotoPerfil: MultipartBody.Part?,
        @Part fotoCarnet: MultipartBody.Part?
    ): Response<Missatge>*/

    //Puts
    @Multipart
    @PUT("/api/usuari_image/{id}")
    suspend fun updateUserImage(
        @Path("id") id: String, @Part f_perfil: MultipartBody.Part?
    ): Response<Missatge>

    @Multipart
    @PUT("/api/usuari_image/{id}")
    suspend fun updateUserImageCarnet(
        @Path("id") id: String, @Part f_perfil: MultipartBody.Part?,
        @Part f_tecnica: MultipartBody.Part?
    ): Response<Missatge>

    @Multipart
    @PUT("/api/cotxe_ftecnic/{matricula}")
    suspend fun updateCotxeFTecnic(@Path("matricula") matricula: String, @Part f_tecnic: MultipartBody.Part?): Response<Missatge>

    @PUT("/api/zona_habilitada/{id}")
    suspend fun updateZonaCuberta(@Path("id") id: String) : Response<Missatge>

    //Gets
    @GET("/api/zones")
    suspend fun getZones(): Response<List<Zona>>

    @GET("/api/zones_comunitats")
    suspend fun getComunitats(): Response<List<String>>

    @GET("/api/zones_provincia/{comunitat}")
    suspend fun getZonesXComunitat(@Path("comunitat") comunitat: String): Response<List<String>>

    @GET("/api/zones_ciutat/{provincia}")
    suspend fun getZonesXProvincia(@Path("provincia") provincia: String): Response<List<Zona>>

    /*@GET("/api/usuari_login")
    suspend fun getUsuariViaContraPass(@Query("email") email: String, @Query("password") password: String): Response<Usuari>*/
    @POST("/api/usuari_login")
    suspend fun loginUsuari(@Body loginRequest: LoginRequest): Response<Usuari>

    @GET("/api/usuari-pagaments/{id_usuari}")
    suspend fun getDadesPagamentByUsuari(@Path("id_usuari") id_usuari: String) : Response<DadesPagament>

}