package com.example.fieldsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fieldsync.VisitItems.VisitLocation
import com.example.fieldsync.VisitItems.VisitLocationAdapter
import com.example.fieldsync.databinding.FragmentStoreManagementBinding

class StoreManagement : Fragment(R.layout.fragment_store_management)  {

    private var _binding: FragmentStoreManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StoreViewModel by viewModels()

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

        println("DEBUG: StoreManagement fragment created")
        setupRecyclerView()
        loadStores()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.visitRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // Updated to load real store data from API instead of hardcoded list
    private fun loadStores() {
        println("DEBUG: Starting to load stores...")
        viewModel.loadStores(
            onResult = { stores ->
                println("DEBUG: Successfully received ${stores.size} stores")

                stores.forEach { store ->
                    println("DEBUG: Store - ${store.name} in ${store.city}")
                }
                // convert API Store objects to VisitLocation formate for existing adapter
                val visitLocation = stores.map { store ->
                    VisitLocation(
                        name = store.name,
                        address = "${store.address}, ${store.city}, ${store.state}",
                        time = "Store Hours: TBD", // placeholder for MVP
                        status = "Active"
                    )
                }
                println("DEBUG: Converted to ${visitLocation.size} VisitLocation objects")
                binding.visitRecyclerView.adapter = VisitLocationAdapter(visitLocation)
                println("DEBUG: Adapter set with data")
            },
                onError = {error ->
                    println("DEBUG: Error occurred - $error")
                        binding.visitRecyclerView.adapter = VisitLocationAdapter(emptyList())
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}