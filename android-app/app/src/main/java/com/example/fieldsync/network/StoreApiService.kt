package com.example.fieldsync.network

import com.example.fieldsync.data.StoreResponse
import retrofit2.http.GET
import retrofit2.http.Path

// API interface definitions for store endpoints
interface StoreApiService {
    @GET("stores/{userId}")
    suspend fun getUserStores(@Path("userId") userId: String): StoreResponse
    @GET("stores/")
    suspend fun getAllStores(): StoreResponse
}