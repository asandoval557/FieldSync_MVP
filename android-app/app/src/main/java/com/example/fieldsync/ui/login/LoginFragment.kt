package com.example.fieldsync.ui.login

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.example.fieldsync.databinding.FragmentLoginBinding
import com.example.fieldsync.R
import com.example.fieldsync.MainActivity
import com.example.fieldsync.MainMenu

class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading
        val signUpButton = binding.signUpButton
        val forgotPasswordLink = binding.linkForgotPassword

        // Observe form state
        loginViewModel.loginFormState.observe(viewLifecycleOwner) { loginFormState ->
            loginFormState ?: return@observe
            loginButton.isEnabled = loginFormState.isDataValid
            loginFormState.usernameError?.let {
                usernameEditText.error = getString(it)
            }
            loginFormState.passwordError?.let {
                passwordEditText.error = getString(it)
            }
        }

        // Observe login result
        loginViewModel.loginResult.observe(viewLifecycleOwner) { loginResult ->
            loginResult ?: return@observe
            loadingProgressBar.visibility = View.GONE

            loginResult.error?.let { showLoginFailed(it) }

            loginResult.success?.let {
                updateUiWithUser(it)
                (requireActivity() as MainActivity).SetActiveFragment(MainMenu())
            }
        }

        // Text watcher for real-time validation
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }

        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loadingProgressBar.visibility = View.VISIBLE
                loginViewModel.login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        // Login button click
        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            loginViewModel.login(
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }

        // Sign Up button click
        signUpButton.setOnClickListener {
            (requireActivity() as MainActivity).SetActiveFragment(SignUpFragment())
        }

        // Forgot Password link click
       /* forgotPasswordLink.setOnClickListener {
            (requireActivity() as MainActivity).SetActiveFragment(ForgotPasswordFragment())
        }*/
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + " " + model.displayName
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
