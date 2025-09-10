package com.example.fieldsync.VisitItems

// Represents a scheduled visit to a store location
data class VisitLocation(
    val name: String,
    val address: String,
    val time: String,
    val status: String
)