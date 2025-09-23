package com.example.fieldsync.VisitItems

import java.util.*

// Represents a scheduled visit to a store location
data class VisitLocation(
    val visit_id: String?,
    val store_id: UUID,
    val store_name: String,
    val address: String,
    val check_in_time: String,
    val check_out_time: String?,
    val duration_minutes: Int?,
    val visit_purpose: String,
    val compliance_status: String,
    val notes: String?
)