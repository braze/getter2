package com.example.getter2.sunset.api

import com.example.getter2.sunset.data.SunsetResult
import retrofit2.http.GET
import retrofit2.http.Query

interface SunsetService {

    @GET("/json")
    suspend fun retrieveSunset(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ): SunsetResult
}