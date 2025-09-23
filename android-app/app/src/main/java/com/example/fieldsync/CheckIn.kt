package com.example.fieldsync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fieldsync.databinding.FragmentCheckInBinding
import java.text.SimpleDateFormat
import java.util.*

class CheckIn : Fragment(R.layout.fragment_check_in) {

    private var _binding: FragmentCheckInBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext().getSharedPreferences("visits", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckInBinding.inflate(inflater, container, false)

        // keep your insets handling
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        restoreState()

        // CHECK IN
        binding.checkInCheckInBtn.setOnClickListener {
            val store = binding.checkInStoreEt.text?.toString()?.trim().orEmpty()
            if (store.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a store location first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val now = System.currentTimeMillis()
            // generate visitId
            val newVisitId = prefs.getInt(KEY_VISIT_ID, 0) + 1
            prefs.edit()
                .putBoolean(KEY_CHECKED_IN, true)
                .putString(KEY_STORE, store)
                .putLong(KEY_START_TIME, now)
                .putLong(KEY_END_TIME, 0L)
                .putInt(KEY_VISIT_ID, newVisitId)   // save visitId
                .apply()

            updateUi(checkedIn = true, store = store, start = now, end = 0L)
        }

        // CHECK OUT
        binding.checkInCheckOutBtn.setOnClickListener {
            val start = prefs.getLong(KEY_START_TIME, 0L)
            val store = prefs.getString(KEY_STORE, "") ?: ""
            if (start == 0L) {
                Toast.makeText(requireContext(), "You’re not checked in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val end = System.currentTimeMillis()
            val dur = end - start

            prefs.edit()
                .putBoolean(KEY_CHECKED_IN, false)
                .putLong(KEY_END_TIME, end)
                .apply()

            appendVisit(store, start, end, dur)
            updateUi(checkedIn = false, store = store, start = start, end = end)
        }

        binding.checkInBackBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun restoreState() {
        val checkedIn = prefs.getBoolean(KEY_CHECKED_IN, false)
        val store = prefs.getString(KEY_STORE, "") ?: ""
        val start = prefs.getLong(KEY_START_TIME, 0L)
        val end = prefs.getLong(KEY_END_TIME, 0L)

        // prefill store box with last used value
        binding.checkInStoreEt.setText(store)
        updateUi(checkedIn, store, start, end)
    }

    private fun updateUi(checkedIn: Boolean, store: String, start: Long, end: Long) = binding.apply {
        // Buttons
        checkInCheckInBtn.isEnabled = !checkedIn
        checkInCheckOutBtn.isEnabled = checkedIn

        // Status + times
        checkInStatusTv.text =
            if (checkedIn) "Status: Checked In @ ${store.ifBlank { "—" }}"
            else "Status: Not Checked In"

        checkInStartTv.text = if (start > 0) "Start: ${fmtTime(start)}" else "Start: —"
        checkInEndTv.text   = if (end   > 0) "End: ${fmtTime(end)}"     else "End: —"

        val dur = if (start > 0 && end > 0) end - start else 0L
        checkInDurationTv.text =
            if (dur > 0) "Visit Duration: ${fmtDuration(dur)}" else "Visit Duration: —"
    }

    private fun fmtTime(t: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.getDefault())
        return sdf.format(Date(t))
    }

    private fun fmtDuration(ms: Long): String {
        var secs = ms / 1000
        val hrs = secs / 3600
        secs %= 3600
        val mins = secs / 60
        secs %= 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }

    private fun appendVisit(store: String, start: Long, end: Long, duration: Long) {
        // simple CSV log; will use in Visit History later
        val existing = prefs.getString(KEY_VISIT_LOG, "") ?: ""
        val line = "${store.ifBlank { "-" }},${fmtTime(start)},${fmtTime(end)},${fmtDuration(duration)}"
        val updated = if (existing.isBlank()) line else "$existing\n$line"
        prefs.edit().putString(KEY_VISIT_LOG, updated).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_CHECKED_IN  = "checked_in"
        private const val KEY_STORE       = "store"
        private const val KEY_START_TIME  = "start_time"
        private const val KEY_END_TIME    = "end_time"
        private const val KEY_VISIT_LOG   = "visit_log"
        private const val KEY_VISIT_ID    = "visit_id"
    }
}
