package com.example.easydrive.api.esaydrive

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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("/api/reserva")
    suspend fun afegirReserva(@Body reserva: Reserva): Response<Missatge>

    /*@Multipart
    @POST("/api/usuari")
    suspend fun insertUser2(
        @Part("usuari") usuari: RequestBody,
        @Part fotoPerfil: MultipartBody.Part?,
        @Part fotoCarnet: MultipartBody.Part?
    ): Response<Missatge>*/

    @POST("/api/viatge")
    suspend fun afegirViatge(@Body viatge: Viatja): Response<Missatge>

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

    @PUT("/api/usuari/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body usuari: Usuari): Response<Missatge>

    @PUT("/api/dades-pagament/{id}")
    suspend fun updateDadesPagament(@Path("id") id: Int, @Body dp: DadesPagament): Response<Missatge>

    @PUT("/api/usuari/canvi-contrasenya")
    suspend fun changePassword(@Query("id") id: String?, @Body request: ChangePasswordRequest): Response<ResponseBody>

    @Multipart
    @PUT("/api/cotxe_ftecnic/{matricula}")
    suspend fun updateCotxeFTecnic(@Path("matricula") matricula: String, @Part f_tecnic: MultipartBody.Part?): Response<Missatge>

    @PUT("/api/zona_habilitada/{id}")
    suspend fun updateZonaCuberta(@Path("id") id: String) : Response<Missatge>

    @PUT("/api/usuari-disponiblitat")
    suspend fun updateDisponiblitat(@Query("id") id: String, @Query("dispo") dispo: Boolean ): Response<Missatge>

    @PUT("/api/reserva/{id}")
    suspend fun updateReserva(@Path("id") id: String, @Body reserva: Reserva): Response<Missatge>

    @PUT("/api/viatge/{id}")
    suspend fun updateViatge(@Path("id") id: Int, @Body viatge: Viatja): Response<Missatge>

    @PUT("/api/cotxe/{id}")
    suspend fun updateCotxe(@Path("id") id: String, @Body cotxe: Cotxe): Response<Missatge>

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

    @GET("/api/disponiblitat-taxista/{id}")
    suspend fun getDispoTaxista(@Path("id") id: String) : Response<Boolean>

    @GET("/api/reserves-usuari/{id_usuari}")
    suspend fun getReservesByUsuari(@Path("id_usuari") id_usuari:String) : Response<MutableList<Reserva>>

    @GET("/api/reserves-pendents")
    suspend fun getReservesPendents() : Response<MutableList<Reserva>>

    @GET("/api/viatges-usuari/{id_usuari}")
    suspend fun getAllViatgesByUser(@Path("id_usuari") id_usuari:String): Response<List<Viatja>?> // es si les reserves estan confirmades

    @GET("/api/viatges-taxista/{id_usuari}")
    suspend fun getAllViatgesByTaxista(@Path("id_usuari") id_usuari:String): Response<List<Viatja>?>

    @GET("/api/usuari/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<Usuari>

    @GET("/api/reserva/{id}")
    suspend fun getReservaById(@Path("id") id: String): Response<Reserva>

    @GET("/api/viatge-reserva/{id}")
    suspend fun getViatgeByReserva(@Path("id") id: Int): Response<Viatja>

    @GET("/api/cotxe/{id}")
    suspend fun getCotxeByMatricula(@Path("id") id: String) : Response<Cotxe>

    @GET("/api/cotxes-taxista/{id_taxista}")
    suspend fun getCotxesTaxista(@Path("id_taxista") id: String) : Response<List<Cotxe>>

    @GET("/api/reserva-confirmada/{id_usuari}/{id_reserva}")
    suspend fun getReservaConfirmada(@Path("id_usuari") idUser: String, @Path("id_reserva") idReserva: String) : Response<Reserva>

    @GET("/api/reserves-confirmats/{id_usuari}")
    suspend fun getReservaConfirmats(@Path("id_usuari") idUser: String) : Response<List<Reserva>>

    //Dels
    @DELETE("api/usuari/del_all/{id_usuari}")
    suspend fun delUser(@Path("id_usuari") id_usuari: String): Response<Missatge>

    @DELETE("/api/reserva/{id}")
    suspend fun delReserva(@Path("id") id: String): Response<Missatge>

    @DELETE("/api/cotxe/{id}")
    suspend fun delCotxe(@Path("id") id: String): Response<Missatge>

    @DELETE("/api/dades-pagament/{id}")
    suspend fun delDadesPagament(@Path("id") id: Int): Response<Missatge>

}