package com.example.fieldsync

import android.content.Context

object VisitUtil {
    fun getCurrentVisitId(context: Context): Long? {
        val prefs = context.getSharedPreferences("visits", Context.MODE_PRIVATE)
        return prefs.getLong("current_visit_id", -1L).takeIf { it != -1L }
    }

    fun getCurrentStoreId(context: Context): Long? {
        val prefs = context.getSharedPreferences("visits", Context.MODE_PRIVATE)
        return prefs.getLong("current_store_id", -1L).takeIf { it != -1L }
    }
}
