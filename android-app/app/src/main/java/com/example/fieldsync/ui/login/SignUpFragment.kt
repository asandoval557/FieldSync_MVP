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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

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


        FirebaseApp.initializeApp(requireContext())
        auth = FirebaseAuth.getInstance()

        binding.signUpButton.setOnClickListener {
            attemptSignUp()
        }

        binding.linkLogin.setOnClickListener {
            (requireActivity() as MainActivity).SetActiveFragment(LoginFragment())
        }
    }

    private fun attemptSignUp() {
        val email = binding.signUpEmail.text?.toString()?.trim().orEmpty()
        val password = binding.signUpPassword.text?.toString().orEmpty()
        val confirm = binding.signUpConfirmPassword.text?.toString().orEmpty()

        // Basic validation
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
            .addOnCompleteListener(requireActivity()) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val emailText = user?.email ?: "user"
                    Toast.makeText(requireContext(), "Account created for $emailText", Toast.LENGTH_SHORT).show()


                    (requireActivity() as MainActivity).SetActiveFragment(MainMenu())
                } else {
                    val msg = task.exception?.localizedMessage ?: "Sign up failed"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { ex ->
                setLoading(false)
                Toast.makeText(requireContext(), "Error: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun isEmailValid(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setLoading(loading: Boolean) {
        binding.signUpLoading.visibility = if (loading) View.VISIBLE else View.GONE
        binding.signUpButton.isEnabled = !loading
        binding.signUpEmail.isEnabled = !loading
        binding.signUpPassword.isEnabled = !loading
        binding.signUpConfirmPassword.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
