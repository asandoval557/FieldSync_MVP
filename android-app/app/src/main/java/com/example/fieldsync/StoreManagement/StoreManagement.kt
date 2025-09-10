package com.example.fieldsync.StoreManagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fieldsync.R
import com.example.fieldsync.databinding.FragmentStoreManagementBinding

class StoreManagement : Fragment(R.layout.fragment_store_management)  {

    private var _binding: FragmentStoreManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreManagementBinding.inflate(inflater, container, false)


        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Sample list of upcoming store visits
        val visitList = listOf(
            VisitLocation("Best Buy - Downtown", "123 Main St, San Antonio, TX",
                "9:00 AM - 10:30 AM", "Upcoming"),
            VisitLocation("Target - Northside", "456 Oak Ave, San Antonio, TX",
                "11:00 AM - 12:30 PM", "Upcoming"),
            VisitLocation("Walmart - Southpark", "789 Elm St, San Antonio, TX",
                "2:00 PM - 3:30 PM", "Upcoming")
        )

        binding.visitRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = VisitLocationAdapter(visitList)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}