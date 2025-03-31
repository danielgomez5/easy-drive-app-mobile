package com.example.easydrive.api

import com.example.easydrive.dades.Missatge
import com.example.easydrive.dades.Usuari
import com.example.easydrive.dades.Zona
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/api/usuari")
    suspend fun insertUser(@Body usuari: Usuari): Response<Missatge>

    @GET("/api/zones")
    suspend fun getZones() : Response<List<Zona>>
}