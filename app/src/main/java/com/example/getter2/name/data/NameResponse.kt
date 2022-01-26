package com.example.getter2.name.data

import com.example.getter2.name.data.Data

data class NameResponse(
    val `data`: List<Data>,
    val error: Any,
    val results: Int
)