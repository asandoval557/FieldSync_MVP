package com.example.fieldsync.api

import com.example.fieldsync.VisitItems.VisitLocation
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.UUID

interface VisitHistoryApi {
    @GET("/visits")
   suspend fun getVisitHistory(
        @Query("user_id") user_id: UUID,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("store_id") store_id: UUID? = null,
    ): List<VisitLocation>
}

