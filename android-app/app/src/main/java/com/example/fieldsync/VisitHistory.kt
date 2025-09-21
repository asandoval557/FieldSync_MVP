package com.example.fieldsync

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fieldsync.VisitItems.VisitLocation
import com.example.fieldsync.VisitItems.VisitLocationAdapter
import com.example.fieldsync.api.RetrofitClient
import com.example.fieldsync.databinding.FragmentVisitHistoryBinding
import kotlinx.coroutines.launch
import java.util.*

class VisitHistory : Fragment(R.layout.fragment_visit_history) {

    private var _binding: FragmentVisitHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout using ViewBinding
        _binding = FragmentVisitHistoryBinding.inflate(inflater, container, false)

        val userId = UUID.fromString("f98f31a9-ab9f-416f-89b0-39531122b9a9")
        lifecycleScope.launch {
            try {
                binding.visitHistoryErrorTxt.text = "Loading..."
                binding.visitHistoryErrorTxt.visibility = View.VISIBLE
                val visits = RetrofitClient.visitHistoryApi.getVisitHistory(userId)
                binding.viewHistoryRecycleView.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = VisitLocationAdapter(visits)
                    binding.visitHistoryErrorTxt.visibility = View.INVISIBLE
                    if(visits.isEmpty()) {
                        binding.visitHistoryErrorTxt.text = "No visits found"
                        binding.visitHistoryErrorTxt.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                binding.visitHistoryErrorTxt.text = "Failed to retrieve past visits"
                binding.visitHistoryErrorTxt.visibility = View.VISIBLE
                Log.e("VisitHistory", "Failed to load visits", e)
            }
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