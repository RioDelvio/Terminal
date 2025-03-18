package com.rio.terminal.data

import retrofit2.http.GET

interface ApiService {

    @GET("aggs/ticker/AAPL/range/30/minute/2023-01-09/2025-02-10?adjusted=true&sort=asc&limit=5000&apiKey=yxslCYtwJApJO6aCcxbV2tJasBY2UMc2")
    suspend fun loadBars(): Result
}