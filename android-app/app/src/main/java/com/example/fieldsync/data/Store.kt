package com.example.fieldsync.data

// Store data classes for API response from backend
data class Store(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zip_code: String,
    val phone: String?,
    val email: String?,
    val store_type: String?
)

data class StoreResponse(
    val user_id: String,
    val store_count: Int,
    val stores: List<Store>
)