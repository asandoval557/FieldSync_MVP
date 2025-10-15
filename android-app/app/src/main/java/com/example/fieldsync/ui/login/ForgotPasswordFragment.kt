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
import com.example.fieldsync.R
import com.example.fieldsync.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.resetPasswordButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()

            if (!isValidEmail(email)) {
                binding.emailInput.error = getString(R.string.invalid_username)
                return@setOnClickListener
            }

            setLoading(true)

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Reset link sent to $email", Toast.LENGTH_LONG).show()
                        (requireActivity() as MainActivity).SetActiveFragment(LoginFragment())
                    } else {
                        val msg = task.exception?.localizedMessage ?: "Failed to send reset email"
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.resetPasswordButton.isEnabled = !isLoading
        binding.emailInput.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
