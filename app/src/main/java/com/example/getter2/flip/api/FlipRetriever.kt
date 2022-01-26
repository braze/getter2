package com.example.getter2.flip.api

import com.example.getter2.flip.data.FlipResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FlipRetriever {

    private val service: YesNoService

    companion object {
        private const val BASE_URL = "https://yesno.wtf/"
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(YesNoService::class.java)
    }

    suspend fun getFlip(): FlipResult {
        return service.retrieveFlipper()
    }
}