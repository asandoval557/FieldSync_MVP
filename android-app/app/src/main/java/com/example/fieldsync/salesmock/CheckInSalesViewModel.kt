package com.example.fieldsync.salesmock

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class CheckInSalesViewModel : ViewModel() {

    private val _series = MutableStateFlow<List<SalesPoint>>(emptyList())
    val series: StateFlow<List<SalesPoint>> = _series.asStateFlow()

    /** For now we always load mock; later you can branch to real data here. */
    fun loadForStore(storeId: Long, today: Date = Date()) {
        _series.value = generateRolling30Days(storeId, today)
    }

    fun clear() {
        _series.value = emptyList()
    }
}


