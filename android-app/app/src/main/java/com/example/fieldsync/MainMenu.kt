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
        binding = FragmentMainMenuBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set temporary placeholder right away
        binding.mainMenuHelloTxt.text = "Hello, loading..."

        val prefs = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE)
        val cachedName = prefs.getString("username", null)
        val currentUser = auth.currentUser

        //  If we have a cached username, show it right away
        if (!cachedName.isNullOrBlank()) {
            binding.mainMenuHelloTxt.text = "Hello, $cachedName"
        }
        // If not cached, but user is logged in, fetch from Firestore
        else if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("username")
                    if (!name.isNullOrBlank()) {
                        binding.mainMenuHelloTxt.text = "Hello, $name"
                        prefs.edit().putString("username", name).apply()
                    } else {
                        binding.mainMenuHelloTxt.text = "Hello, User"
                    }
                }
                .addOnFailureListener { e ->
                    binding.mainMenuHelloTxt.text = "Hello, User"
                    Toast.makeText(requireContext(), "Error loading username: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        //No user logged in
        else {
            binding.mainMenuHelloTxt.text = "Hello, Guest"
        }

        // --- Buttons ---
        binding.mainMenuCheckInBtn.setOnClickListener {
            (activity as? MainActivity)?.SetActiveFragment(CheckIn())
        }

        binding.mainMenuStoreManagementBtn.setOnClickListener {
            (activity as? MainActivity)?.SetActiveFragment(StoreManagement())
        }

        binding.mainMenuVisitHistoryBtn.setOnClickListener {
            (activity as? MainActivity)?.SetActiveFragment(VisitHistory())
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
            (activity as? MainActivity)?.SetActiveFragment(PhotoCapture())
        }

        binding.mainMenuLogoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            prefs.edit().clear().apply() // clear cached username
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.SetActiveFragment(LoginFragment())
        }
    }
}
