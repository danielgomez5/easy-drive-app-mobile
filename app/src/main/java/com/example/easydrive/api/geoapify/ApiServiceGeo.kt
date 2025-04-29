package com.example.easydrive.api.geoapify

import com.example.easydrive.dades.GeoapifyDades
import com.example.easydrive.dades.GeoapifyResponse
import retrofit2.Response

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServiceGeo {
    @GET("v1/geocode/search?format=json&limit=100")
    suspend fun buscarDesti(
        @Query("text") text: String,
        @Query("apiKey") apiKey: String
    ): Response<GeoapifyResponse>

    @GET("v1/geocode/reverse?format=json&limit=100")
    suspend fun buscarDestiLatLon(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("apiKey") apiKey: String
    ): Response<GeoapifyResponse>
}
