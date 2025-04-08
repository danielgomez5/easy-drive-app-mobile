package com.example.easydrive.api

import com.example.easydrive.dades.OpenRouteDades
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServiceOpenRoute {
    @GET("/v2/directions/driving-car")
    suspend fun getRutaEnCoche(@Query("api_key") api_key:String, @Query("start") start:String, @Query("end") fi:String): Response<OpenRouteDades>
}