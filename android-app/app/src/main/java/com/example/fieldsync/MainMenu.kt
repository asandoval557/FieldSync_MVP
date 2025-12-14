package com.example.fieldsync

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fieldsync.databinding.FragmentMainMenuBinding
import com.example.fieldsync.ui.login.LoginFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainMenu : Fragment(R.layout.fragment_main_menu) {

    private lateinit var binding: FragmentMainMenuBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainMenuBinding.bind(view)

        // Apply system bar insets to root
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // SharedPreferences still used for clearing username on logout
        val prefs = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE)

        // --- Buttons ---
        binding.mainMenuCheckInBtn.setOnClickListener {
            (activity as? MainActivity)?.SetActiveFragment(CheckIn())
        }

        binding.mainMenuStoreManagementBtn.setOnClickListener {
            (activity as? MainActivity)?.SetActiveFragment(StoreManagement())
        }

        binding.mainMenuVisitHistoryBtn.setOnClickListener {
            val visitPrefs = requireContext().getSharedPreferences("visits", Context.MODE_PRIVATE)
            val visitId = VisitUtil.getCurrentVisitId(requireContext())
            val storeName = visitPrefs.getString("current_store_name", null)

            if (visitId != null && !storeName.isNullOrEmpty()) {
                (activity as? MainActivity)?.SetActiveFragment(VisitHistory())
            } else {
                Toast.makeText(requireContext(), "Please check in to a store first.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.mainMenuVisitNotesBtn.setOnClickListener {
            val visitPrefs = requireContext().getSharedPreferences("visits", Context.MODE_PRIVATE)
            val visitId = VisitUtil.getCurrentVisitId(requireContext())
            val storeName = visitPrefs.getString("current_store_name", null)

            if (visitId != null && !storeName.isNullOrEmpty()) {
                (activity as? MainActivity)?.SetActiveFragment(VisitNotes())
            } else {
                Toast.makeText(requireContext(), "Please check in to a store first.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.mainMenuPhotoCaptureBtn.setOnClickListener {
            val visitPrefs = requireContext().getSharedPreferences("visits", Context.MODE_PRIVATE)
            val visitId = VisitUtil.getCurrentVisitId(requireContext())
            val storeName = visitPrefs.getString("current_store_name", null)

            if (visitId != null && !storeName.isNullOrEmpty()) {
                (activity as? MainActivity)?.SetActiveFragment(PhotoCapture())
            } else {
                Toast.makeText(requireContext(), "Please check in to a store first.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.mainMenuLogoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            prefs.edit().clear().apply() // clear cached username (and other user prefs)
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.SetActiveFragment(LoginFragment())
        }
    }
}
