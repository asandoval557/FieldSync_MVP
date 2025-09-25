package com.example.fieldsync.VisitItems

import com.google.firebase.Timestamp
import java.util.*

// Represents a scheduled visit to a store location
data class Visit(
    val CheckIn: Timestamp? = null,
    val CheckOut: Timestamp? = null,
    val Status: String = "",
    val StoreID: Int = 0,
    val StoreName: String = "",
    val VisitDuration: Int = 0,
    val VisitID: Long = 0L

)