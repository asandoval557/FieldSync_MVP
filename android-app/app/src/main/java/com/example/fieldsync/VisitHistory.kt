package com.example.fieldsync

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fieldsync.VisitItems.VisitLocation
import com.example.fieldsync.VisitItems.VisitLocationAdapter
import com.example.fieldsync.databinding.FragmentVisitHistoryBinding

class VisitHistory : Fragment(R.layout.fragment_visit_history) {

    private var _binding: FragmentVisitHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout using ViewBinding
        _binding = FragmentVisitHistoryBinding.inflate(inflater, container, false)

        // Sample visit history data
        val visitHistoryList = listOf(
            VisitLocation("Staples", "123 Main St, San Antonio, TX",
                "9:00 AM - 10:30 AM", "Completed"),
            VisitLocation("Game Stop - Northside", "456 Oak Ave, San Antonio, TX",
                "11:00 AM - 12:30 PM", "Completed")
        )

        // Set up RecyclerView with adapter and layout manager
        binding.viewHistoryRecycleView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = VisitLocationAdapter(visitHistoryList)
        }

        binding.visitHistoryBackBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}