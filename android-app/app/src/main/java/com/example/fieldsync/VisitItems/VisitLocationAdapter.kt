package com.example.fieldsync.VisitItems

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fieldsync.databinding.ItemVisitLocationBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class VisitLocationAdapter(
    private val visitLocations: List<Visit>
) : RecyclerView.Adapter<VisitLocationAdapter.VisitViewHolder>() {

    inner class VisitViewHolder(
        val binding: ItemVisitLocationBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val binding = ItemVisitLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VisitViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return visitLocations.size
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {

        if (visitLocations.isEmpty()) {
            with(holder.binding) {
                storeStoreNameTxt.text = "No past visits found"
                storeStoreAddressTxt.text = ""
                storeStoreTimeTxt.text = ""
                storeStoreStatusTxt.text = ""
                storeStoreDurationTxt.text = ""
            }
            return
        }

        val visit = visitLocations[position]
        with(holder.binding) {
            storeStoreNameTxt.text = visit.StoreName

            getStoreAddress(visit.StoreID, holder)

            val formatter = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())

            val timeRange = if (visit.CheckIn != null && visit.CheckOut != null) {
                "${formatter.format(visit.CheckIn.toDate())} - ${formatter.format( visit.CheckOut.toDate())}"
            } else {
                "Time not available"
            }

            storeStoreTimeTxt.text = timeRange
            storeStoreStatusTxt.text = "Complete"

            val duration = "Duration:  ${visit.VisitDuration}"
            storeStoreDurationTxt.text = duration

            // Change color depending on status
            val statusColor = when (visit.Status.lowercase()) {
                "checked_out" -> ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
                "checked_in" -> ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark)
                else -> ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
            }
            storeStoreStatusTxt.setTextColor(statusColor)
        }
    }

   private fun getStoreAddress(storeID: Int, holder: VisitViewHolder) {
        Firebase.firestore.collection("Store_Management")
            .whereEqualTo("StoreID", storeID)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents[0]
                    val address = doc.getString("Address") ?: ""
                    val city = doc.getString("City") ?: ""
                    val state = doc.getString("State") ?: ""
                    val fullAddress = "$address, $city, $state"

                    holder.binding.storeStoreAddressTxt.text = fullAddress
                } else {
                    holder.binding.storeStoreAddressTxt.text = "Address not found"
                }
            }
            .addOnFailureListener {
                holder.binding.storeStoreAddressTxt.text = "Error loading address"
                Log.e("VisitHistory", "Failed to fetch store address", it)
            }

    }

}