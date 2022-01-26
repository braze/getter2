package com.example.getter2.name.api

import com.example.getter2.name.data.NameResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface NameService {

    @GET("/?")
    suspend fun getNameInfo(
        @Query("api_key") api_key: String,
        @Query("endpoint") endpoint: String,
        @Query("name") name: String
    ): NameResponse


}