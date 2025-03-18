package com.rio.terminal.data

import retrofit2.http.GET

interface ApiService {

    @GET("v2/aggs/ticker/AAPL/range/30/minute/2023-01-09/2024-01-09?adjusted=true&sort=desc&limit=50000&apiKey=yxslCYtwJApJO6aCcxbV2tJasBY2UMc2")
    suspend fun loadBars(): Result
}