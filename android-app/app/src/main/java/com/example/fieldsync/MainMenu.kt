package com.example.fieldsync

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fieldsync.databinding.FragmentMainMenuBinding

class MainMenu : Fragment(R.layout.fragment_main_menu) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentMainMenuBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.mainMenuCheckInBtn.setOnClickListener {
            val activity = activity as MainActivity?
            activity?.SetActiveFragment(CheckIn())
        }

        binding.mainMenuStoreManagementBtn.setOnClickListener {
            val activity = activity as MainActivity?
            activity?.SetActiveFragment(StoreManagement())
        }

        binding.mainMenuVisitHistoryBtn.setOnClickListener {
            val activity = activity as MainActivity?
            activity?.SetActiveFragment(VisitHistory())
        }

        binding.mainMenuVisitNotesBtn.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("visits", Context.MODE_PRIVATE)
            val visitId = VisitUtil.getCurrentVisitId(requireContext())
            val storeName = prefs.getString("current_store_name", null)

            if (visitId != null && !storeName.isNullOrEmpty()) {
                val activity = activity as MainActivity?
                activity?.SetActiveFragment(VisitNotes())
            } else {
                Toast.makeText(requireContext(), "Please check in to a store first.", Toast.LENGTH_SHORT).show()

            }
        }

        binding.mainMenuPhotoCaptureBtn.setOnClickListener {
            val activity = activity as MainActivity?
            activity?.SetActiveFragment(PhotoCapture())
        }
    }
}
