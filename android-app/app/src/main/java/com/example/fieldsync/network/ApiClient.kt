package com.example.fieldsync.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//Retrofit client for connecting to FastApi backend
object ApiClient {
    // Android emulator localhost access
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val storeApiService: StoreApiService = retrofit.create(StoreApiService::class.java)
}