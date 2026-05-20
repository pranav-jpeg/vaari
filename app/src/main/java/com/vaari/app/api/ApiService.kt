package com.vaari.app.api

import com.vaari.app.model.CropResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("crop")
    suspend fun searchCrop(
        @Query("name") name: String
    ): Response<CropResponse>

    @GET("suggest")
    suspend fun getSuggestions(@Query("q") query: String): Response<List<String>>
}