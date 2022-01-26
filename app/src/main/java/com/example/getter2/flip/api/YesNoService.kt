package com.example.getter2.flip.api

import com.example.getter2.flip.data.FlipResult
import retrofit2.http.GET

interface YesNoService {

    @GET("/api")
    suspend fun retrieveFlipper(): FlipResult
}