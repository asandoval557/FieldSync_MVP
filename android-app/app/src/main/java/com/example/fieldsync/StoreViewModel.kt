package com.example.fieldsync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fieldsync.data.Store
import com.example.fieldsync.network.ApiClient
import kotlinx.coroutines.launch

//ViewModel to handle store data loading from API
class StoreViewModel : ViewModel() {

    fun loadStores(onResult: (List<Store>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                println("DEBUG: Making API call to backend...")
                val demoUserId = "4146c3ea-10f9-45e7-b2ae-51e5941fe614"
                val response = ApiClient.storeApiService.getUserStores(demoUserId)
                println("DEBUG: API call successful, received response")
                onResult(response.stores)
            } catch (e: Exception) {
                println("DEBUG: API call failed - ${e.message}")
                e.printStackTrace()
                onError(e.message ?: "Failed to load stores")
            }
        }
    }
}