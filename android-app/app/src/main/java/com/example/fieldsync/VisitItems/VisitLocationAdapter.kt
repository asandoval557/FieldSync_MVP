package com.example.fieldsync.VisitItems

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fieldsync.databinding.ItemVisitLocationBinding
import java.text.SimpleDateFormat
import java.util.*

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

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visitLocations[position]
        with(holder.binding) {
            storeStoreNameTxt.text = visit.store_name
            storeStoreAddressTxt.text = visit.address
            storeStoreTimeTxt.text = formatTimeRange(visit.check_in_time, visit.check_out_time)
            storeStoreStatusTxt.text = visit.compliance_status
            storeStoreDurationTxt.text = visit.duration_minutes?.let { convertMinutesToHHMM(it) }

            val statusColor = when (visit.compliance_status.lowercase()) {
                "completed" -> ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
                "upcoming" -> ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark)
                else -> ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
            }
            storeStoreStatusTxt.setTextColor(statusColor)
        }
    }

    // Formats a time range from UTC timestamps into local AM/PM display
    private fun formatTimeRange(start: String, end: String?): String {

        val parsedStart = parseFlexibleTimestamp(start)
        val parsedEnd = end?.let { parseFlexibleTimestamp(it) }

        val outputFormat = SimpleDateFormat("h:mm a", Locale.US)
        outputFormat.timeZone = TimeZone.getDefault()


        val startTime = parsedStart?.let { outputFormat.format(it) }
        val endTime = parsedEnd?.let { outputFormat.format(it) }


        return if (startTime != null && endTime != null) "$startTime - $endTime"
        else if (startTime != null) "$startTime - ongoing"
        else "—"
    }

    // Parses a timestamp string using multiple fallback formats to handle microseconds, milliseconds, or no fractional seconds
    fun parseFlexibleTimestamp(timestamp: String): Date? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", // microseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSS",    // milliseconds
            "yyyy-MM-dd'T'HH:mm:ss"         // no fractional seconds
        )

        // Try each format until one succeeds
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(timestamp)
            } catch (_: Exception) {
                // Ignore and try next format
            }
        }

        // Return null if none of the formats match
        return null
    }


    fun convertMinutesToHHMM(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}:${minutes.toString().padStart(2, '0')}"
    }

}