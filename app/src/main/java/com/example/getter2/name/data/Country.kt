package com.example.getter2.name.data

import com.google.gson.JsonObject

data class Country(
    val alternative_countries: JsonObject,
    val continent: String,
    val country_certainty: Int,
    val country_code: String,
    val country_code_alpha: String,
    val currency: String,
    val demonym: String,
    val name: String,
    val primary_language: String,
    val primary_language_code: String
)