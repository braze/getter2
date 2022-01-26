package com.example.getter2.name.data

import com.google.gson.JsonObject

data class Firstname(
    val alternative_countries: JsonObject,
    val country_certainty: Int,
    val country_code: String,
    val country_frequency: Int,
    val country_rank: Int,
    val gender: String,
    val gender_deviation: Int,
    val gender_formatted: String,
    val name: String,
    val name_ascii: String,
    val unisex: Boolean,
    val validated: Boolean
)