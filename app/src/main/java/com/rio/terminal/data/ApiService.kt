package com.rio.terminal.data

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("v2/aggs/ticker/AAPL/range/{timeFrame}/2023-01-09/2024-01-09?adjusted=true&sort=desc&limit=50000&apiKey=yxslCYtwJApJO6aCcxbV2tJasBY2UMc2")
    suspend fun loadBars(
        @Path("timeFrame") timeFrame: String
    ): Result
}