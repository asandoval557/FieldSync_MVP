package com.example.fieldsync.ui.login

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fieldsync.MainActivity
import com.example.fieldsync.MainMenu
import com.example.fieldsync.R
import com.example.fieldsync.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.signUpButton.setOnClickListener {
            attemptSignUp()
        }

        binding.linkLogin.setOnClickListener {
            (requireActivity() as MainActivity).SetActiveFragment(LoginFragment())
        }
    }

    private fun attemptSignUp() {
        val username = binding.signUpUsername.text?.toString()?.trim().orEmpty()
        val email = binding.signUpEmail.text?.toString()?.trim().orEmpty()
        val password = binding.signUpPassword.text?.toString().orEmpty()
        val confirm = binding.signUpConfirmPassword.text?.toString().orEmpty()

        if (username.isEmpty()) {
            binding.signUpUsername.error = "Please enter a username"
            return
        }
        if (!isEmailValid(email)) {
            binding.signUpEmail.error = getString(R.string.invalid_username)
            return
        }
        if (password.length < 6) {
            binding.signUpPassword.error = "Password must be at least 6 characters"
            return
        }
        if (password != confirm) {
            binding.signUpConfirmPassword.error = "Passwords do not match"
            return
        }

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    // Update display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                    user?.updateProfile(profileUpdates)

                    // Save to Firestore
                    val userMap = hashMapOf(
                        "uid" to userId,
                        "username" to username,
                        "email" to email
                    )

                    if (userId != null) {
                        db.collection("users").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                // Save locally
                                val prefs = requireContext().getSharedPreferences("user", android.content.Context.MODE_PRIVATE)
                                prefs.edit().putString("username", username).apply()

                                Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show()
                                (requireActivity() as MainActivity).SetActiveFragment(MainMenu())
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Firestore error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    val msg = task.exception?.localizedMessage ?: "Sign up failed"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun isEmailValid(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setLoading(loading: Boolean) {
        binding.signUpLoading.visibility = if (loading) View.VISIBLE else View.GONE
        binding.signUpButton.isEnabled = !loading
        binding.signUpUsername.isEnabled = !loading
        binding.signUpEmail.isEnabled = !loading
        binding.signUpPassword.isEnabled = !loading
        binding.signUpConfirmPassword.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
