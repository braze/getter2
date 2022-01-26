package com.example.getter2.name.api


import com.example.getter2.BuildConfig
import com.example.getter2.name.data.NameResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class NameRetriever {

    private val service: NameService

    companion object {
        private const val BASE_URL = "https://api.parser.name"
        private const val API_KEY = BuildConfig.NAME_API_KEY
    }

    var gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        service = retrofit.create(NameService::class.java)
    }

    suspend fun getNameInformation(name: String): NameResponse {
        return service.getNameInfo(API_KEY, "parse", name)
    }


}