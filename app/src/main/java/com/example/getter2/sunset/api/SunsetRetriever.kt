package com.example.getter2.sunset.api

import com.example.getter2.sunset.data.SunsetResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SunsetRetriever {

    private val service: SunsetService

    companion object {
        private const val BASE_URL = "https://api.sunrise-sunset.org"
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(SunsetService::class.java)
    }

    suspend fun getSunset(latitude : Double, longitude : Double): SunsetResult {
        return service.retrieveSunset(latitude, longitude)
    }
}