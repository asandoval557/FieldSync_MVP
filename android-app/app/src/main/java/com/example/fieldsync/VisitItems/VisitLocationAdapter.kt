package com.example.fieldsync.VisitItems

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fieldsync.databinding.ItemVisitLocationBinding

class VisitLocationAdapter(
    private val visitLocations: List<VisitLocation>
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

    override fun onBindViewHolder(holder: VisitViewHolder, positon: Int) {
        with(holder) {
            with(visitLocations[positon]) {
                binding.storeStoreNameTxt.text = this.name
                binding.storeStoreAddressTxt.text = this.address
                binding.storeStoreTimeTxt.text = this.time

                // Set color based on status
                val statusColor = when (this.status.lowercase()) {
                    "completed" -> ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
                    "upcoming" -> ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark)
                    else -> ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
                }
                holder.binding.storeStoreStatusTxt.setTextColor(statusColor)
                binding.storeStoreStatusTxt.text = this.status

            }
        }
    }


}