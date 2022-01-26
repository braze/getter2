package com.example.getter2.sunset.data

data class SunsetResult(val results: SunriseResult)

data class SunriseResult(val sunrise: String,
                         val sunset: String,
                         val solar_noon : String,
                         val day_length : String)

