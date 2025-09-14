package com.example.fieldsync

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fieldsync.databinding.FragmentCheckInBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

class CheckIn : Fragment(R.layout.fragment_check_in) {
    private var _binding: FragmentCheckInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckInBinding.inflate(inflater, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = requireContext().getSharedPreferences("user_status", Context.MODE_PRIVATE)
        var hasCheckedIn = prefs.getBoolean("hasCheckedIn", false)

        // Set initial button text
        binding.checkInCheckInBtn.text = if (hasCheckedIn) "Check-Out" else "Check-In"
        binding.checkInTimeTxtView.text = if (hasCheckedIn) "Check-Out Time" else "Check-In Time"

        // Handles Check-In / Check-Out Button logic
        binding.checkInCheckInBtn.setOnClickListener {
            hasCheckedIn = prefs.getBoolean("hasCheckedIn", false)

            if (hasCheckedIn) {
                // Handle check-out
                prefs.edit().putBoolean("hasCheckedIn", false).apply()
                binding.checkInCheckInBtn.text = "Check-In"
                binding.checkInTimeTxtView.text = "Check-In Time"

                val rawTimeText = binding.checkInTimeTxt.text.toString().trim()
                val normalizedTimeText = rawTimeText.uppercase()

                try {
                    val formatter = DateTimeFormatter.ofPattern("h:mma", Locale.US)
                    val localTime = LocalTime.parse(normalizedTimeText, formatter)

                    val totalMinutes = localTime.hour * 60 + localTime.minute
                    Toast.makeText(requireContext(), "Checked out successfully", Toast.LENGTH_SHORT).show()

                    //  Retrieve check-in time and calculate duration
                    val checkInMinutes = prefs.getInt("checkInMinutes", -1)
                    if (checkInMinutes != -1) {
                        val duration = totalMinutes - checkInMinutes

                        Toast.makeText(requireContext(), "Duration: $duration minutes", Toast.LENGTH_LONG).show()
                    }

                } catch (e: DateTimeParseException) {
                    Toast.makeText(requireContext(), "Invalid time format. Use h:mma like 2:45pm", Toast.LENGTH_SHORT).show()
                }


            } else {
                // Handle check-in
                prefs.edit().putBoolean("hasCheckedIn", true).apply()
                binding.checkInCheckInBtn.text = "Check-Out"
                binding.checkInTimeTxtView.text = "Check-Out Time"

                val rawTimeText = binding.checkInTimeTxt.text.toString().trim()
                val normalizedTimeText = rawTimeText.uppercase()

                try {
                    val formatter = DateTimeFormatter.ofPattern("h:mma", Locale.US)
                    val localTime = LocalTime.parse(normalizedTimeText, formatter)

                    val totalMinutes = localTime.hour * 60 + localTime.minute
                    prefs.edit().putInt("checkInMinutes", totalMinutes).apply()
                    Toast.makeText(requireContext(), "Checked in successfully", Toast.LENGTH_SHORT).show()

                } catch (e: DateTimeParseException) {
                    Toast.makeText(requireContext(), "Invalid time format. Use h:mma like 2:45pm", Toast.LENGTH_SHORT).show()
                }

            }
        }

        binding.checkInBackBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}