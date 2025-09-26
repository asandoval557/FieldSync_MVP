// VisitHistory.kt
package com.example.fieldsync

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fieldsync.databinding.FragmentVisitHistoryBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

class VisitHistory : Fragment() {  // ← we inflate with binding (no layout in constructor)

    private var _binding: FragmentVisitHistoryBinding? = null
    private val binding get() = _binding!!

    private companion object {
        const val TAG = "VisitHistory"
        const val COLLECTION = "Visit_Check_In_Out"
        const val FIELD_STORE_ID = "StoreID"
        const val FIELD_CHECK_IN = "CheckIn"
        const val FIELD_VISIT_DURATION = "VisitDuration"
        const val FIELD_STORE_NAME = "StoreName"
        const val FIELD_STATUS = "Status"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitHistoryBinding.inflate(inflater, container, false)

        // UI wiring
        binding.visitHistoryBackBtn.setOnClickListener { parentFragmentManager.popBackStack() }

        // RecyclerView once
        binding.viewHistoryRecycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.viewHistoryRecycleView.setHasFixedSize(true)

        loadVisitHistory()
        return binding.root
    }

    private fun loadVisitHistory() {
        val currentStoreId = getCurrentStoreId()
        if (currentStoreId == null) {
            showError("You must be checked into a store to view its visit history")
            return
        }

        showLoading(true)
        Log.d(TAG, "Loading visit history for StoreID: $currentStoreId")

        Firebase.firestore.collection(COLLECTION)
            .whereEqualTo(FIELD_STORE_ID, currentStoreId)
            .whereEqualTo(FIELD_STATUS, "checked_out")
            .orderBy(FIELD_CHECK_IN, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val visits = documents.mapNotNull { document ->
                    try {
                        val checkIn = document.getTimestamp(FIELD_CHECK_IN)
                        val duration = document.getLong(FIELD_VISIT_DURATION) ?: 0L
                        val storeName = document.getString(FIELD_STORE_NAME) ?: "Unknown Store"
                        if (checkIn != null) VisitRecord(checkIn, duration, storeName) else null
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing visit document", e)
                        null
                    }
                }

                Log.d(TAG, "Found ${visits.size} visits for store $currentStoreId")
                showLoading(false)
                displayVisits(visits)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading visit history", e)
                showLoading(false)
                showError("Failed to load visit history: ${e.message}")
            }
    }

    private fun displayVisits(visits: List<VisitRecord>) {
        if (visits.isEmpty()) {
            showError("No completed visits found for this store")
            return
        }

        // Set a fresh adapter with the data and show the list
        binding.viewHistoryRecycleView.adapter = VisitHistoryAdapter(visits)
        binding.viewHistoryRecycleView.visibility = View.VISIBLE
        binding.visitHistoryErrorTxt.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.visitHistoryErrorTxt.text = "Loading visit history..."
            binding.visitHistoryErrorTxt.visibility = View.VISIBLE
            binding.viewHistoryRecycleView.visibility = View.GONE
        } else {
            binding.visitHistoryErrorTxt.visibility = View.GONE
            binding.viewHistoryRecycleView.visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        binding.visitHistoryErrorTxt.text = message
        binding.visitHistoryErrorTxt.visibility = View.VISIBLE
        binding.viewHistoryRecycleView.visibility = View.GONE
    }

    private fun getCurrentStoreId(): Long? {
        val prefs = requireContext().getSharedPreferences("visits", Context.MODE_PRIVATE)
        return prefs.getLong("current_store_id", -1L).takeIf { it != -1L }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ---------- Data model ----------
data class VisitRecord(
    val checkInTime: Timestamp,
    val duration: Long, // minutes
    val storeName: String
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            .format(checkInTime.toDate())

    val formattedDuration: String
        get() {
            val m = duration
            return when {
                m < 60 -> "$m min"
                m < 1440 -> {
                    val h = m / 60
                    val r = m % 60
                    if (r == 0L) "${h}h" else "${h}h ${r}m"
                }
                else -> {
                    val d = m / 1440
                    val r = (m % 1440) / 60
                    if (r == 0L) "${d}d" else "${d}d ${r}h"
                }
            }
        }
}

// ---------- RecyclerView Adapter ----------
class VisitHistoryAdapter(
    private val visits: List<VisitRecord>
) : RecyclerView.Adapter<VisitHistoryAdapter.VisitViewHolder>() {

    class VisitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storeText: TextView = itemView.findViewById(R.id.visit_store_text)
        val dateText: TextView = itemView.findViewById(R.id.visit_date_text)
        val durationText: TextView = itemView.findViewById(R.id.visit_duration_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visit_history, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]
        holder.storeText.text = visit.storeName
        holder.dateText.text = visit.formattedDate
        holder.durationText.text = visit.formattedDuration
    }

    override fun getItemCount(): Int = visits.size
}
