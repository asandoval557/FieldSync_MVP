package com.example.fieldsync

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fieldsync.VisitItems.Visit
import com.example.fieldsync.VisitItems.VisitLocationAdapter
import com.example.fieldsync.api.RetrofitClient
import com.example.fieldsync.databinding.FragmentVisitHistoryBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VisitHistory : Fragment(R.layout.fragment_visit_history) {

    private var _binding: FragmentVisitHistoryBinding? = null
    private val binding get() = _binding!!

    // firebase constants
    private companion object {
        const val COLLECTION = "Visit_Check_In_Out"
        const val STORE_COLLECTION = "Store_Management"
        const val FIELD_STORE_ID = "StoreID"
        const val FIELD_STORE_NAME = "StoreName"
        const val FIELD_CHECK_IN = "CheckIn"
        const val FIELD_CHECK_OUT = "CheckOut"
        const val FIELD_VISIT_DURATION = "VisitDuration"
        const val FIELD_VISIT_ID = "VisitID"
        const val FIELD_STATUS = "Status"

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout using ViewBinding
        _binding = FragmentVisitHistoryBinding.inflate(inflater, container, false)

        binding.visitHistoryBackBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.visitHistoryErrorTxt.visibility = View.INVISIBLE

        binding.visitHistoryErrorTxt.text = "Loading..."
        binding.visitHistoryErrorTxt.visibility = View.VISIBLE


        Firebase.firestore.collection(COLLECTION)
            .whereEqualTo(FIELD_STATUS, "checked_out")
            //.whereEqualTo(, currentUserId)  // For user ID
            .get()
            .addOnSuccessListener { result ->
                val visits = result.documents.mapNotNull { it.toObject(Visit::class.java) }
                if (visits.isEmpty()) {
                    binding.visitHistoryErrorTxt.text = "No visits found"
                    binding.visitHistoryErrorTxt.visibility = View.VISIBLE
                }
                else {
                    binding.viewHistoryRecycleView.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = VisitLocationAdapter(visits)
                        binding.visitHistoryErrorTxt.visibility = View.INVISIBLE
                    }
                }
            }
            .addOnFailureListener {
                binding.visitHistoryErrorTxt.text = "Failed to load visits!"
                binding.visitHistoryErrorTxt.visibility = View.VISIBLE
                Log.e("VisitHistory", "Failed to load visits", it)
            }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}