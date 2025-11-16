package com.example.fieldsync.ui.login

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
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

        // Setup checkbox listeners
        setupCheckboxValidation()

        // Setup policy link clicks
        binding.privacyPolicyLink.setOnClickListener {
            showPolicyDialog(PolicyType.PRIVACY)
        }

        binding.termsConditionsLink.setOnClickListener {
            showPolicyDialog(PolicyType.TERMS)
        }

        binding.signUpButton.setOnClickListener {
            attemptSignUp()
        }

        binding.linkLogin.setOnClickListener {
            (requireActivity() as MainActivity).SetActiveFragment(LoginFragment())
        }
    }

    private fun setupCheckboxValidation() {
        val updateButtonState = {
            val privacyChecked = binding.privacyPolicyCheckbox.isChecked
            val termsChecked = binding.termsConditionsCheckbox.isChecked
            binding.signUpButton.isEnabled = privacyChecked && termsChecked
        }

        binding.privacyPolicyCheckbox.setOnCheckedChangeListener { _, _ ->
            updateButtonState()
        }

        binding.termsConditionsCheckbox.setOnCheckedChangeListener { _, _ ->
            updateButtonState()
        }

        // Initial state - button disabled
        binding.signUpButton.isEnabled = false
    }

    private fun showPolicyDialog(type: PolicyType) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_policy)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val title = dialog.findViewById<TextView>(R.id.dialogPolicyTitle)
        val content = dialog.findViewById<TextView>(R.id.dialogPolicyContent)
        val closeButton = dialog.findViewById<ImageButton>(R.id.dialogCloseButton)

        when (type) {
            PolicyType.PRIVACY -> {
                title.text = "Privacy Policy"
                content.text = getPrivacyPolicyText()
            }
            PolicyType.TERMS -> {
                title.text = "Terms and Conditions"
                content.text = getTermsAndConditionsText()
            }
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getPrivacyPolicyText(): String {
        return """
            PRIVACY POLICY
            
            Last updated: ${getCurrentDate()}
            
            1. INFORMATION WE COLLECT
            
            We collect information that you provide directly to us, including:
            • Account information (username, email, password)
            • Store visit data and check-in records
            • Photos taken during store visits
            • Location data when you check into stores
            
            2. HOW WE USE YOUR INFORMATION
            
            We use the information we collect to:
            • Provide and maintain our service
            • Track your store visits and activities
            • Analyze usage patterns to improve our service
            • Communicate with you about your account
            
            3. DATA STORAGE
            
            Your data is stored securely using Firebase services:
            • Authentication data is encrypted
            • Photos are stored locally on your device
            • Visit records are stored in Firestore database
            
            4. DATA SHARING
            
            We do not sell or share your personal information with third parties except:
            • With your explicit consent
            • To comply with legal obligations
            • To protect our rights and safety
            
            5. YOUR RIGHTS
            
            You have the right to:
            • Access your personal data
            • Request deletion of your data
            • Update or correct your information
            • Opt out of data collection
            
            6. SECURITY
            
            We implement appropriate security measures to protect your data, including:
            • Encrypted connections
            • Secure authentication
            • Regular security updates
            
            7. CHILDREN'S PRIVACY
            
            Our service is not intended for users under 13 years of age. We do not knowingly collect information from children.
            
            8. CHANGES TO THIS POLICY
            
            We may update this privacy policy from time to time. We will notify you of any changes by posting the new policy on this page.
            
            9. CONTACT US
            
            If you have questions about this privacy policy, please contact us at:
            support@fieldsync.com
            
            By using FieldSync, you agree to this privacy policy.
        """.trimIndent()
    }

    private fun getTermsAndConditionsText(): String {
        return """
            TERMS AND CONDITIONS
            
            Last updated: ${getCurrentDate()}
            
            1. ACCEPTANCE OF TERMS
            
            By creating an account and using FieldSync, you agree to be bound by these Terms and Conditions. If you do not agree to these terms, do not use our service.
            
            2. USER ACCOUNTS
            
            • You must provide accurate information when creating an account
            • You are responsible for maintaining the security of your account
            • You must be at least 13 years old to use this service
            • One person may not maintain multiple accounts
            
            3. ACCEPTABLE USE
            
            You agree NOT to:
            • Use the service for any illegal purposes
            • Upload inappropriate or offensive content
            • Attempt to access other users' accounts
            • Interfere with the proper functioning of the service
            • Abuse or harass other users
            
            4. STORE VISITS AND CHECK-INS
            
            • You must check in only at stores you are physically visiting
            • False check-ins or fraudulent visit data may result in account termination
            • Visit duration data must be accurate
            • Photos must be relevant to the store visit
            
            5. CONTENT OWNERSHIP
            
            • You retain ownership of photos and content you upload
            • By uploading content, you grant us a license to store and display it
            • We may remove content that violates these terms
            
            6. SERVICE AVAILABILITY
            
            • We strive to keep the service available 24/7
            • We do not guarantee uninterrupted service
            • We may perform maintenance that temporarily limits access
            • We reserve the right to modify or discontinue features
            
            7. TERMINATION
            
            We may terminate or suspend your account if you:
            • Violate these terms
            • Engage in fraudulent activity
            • Abuse the service
            • Fail to comply with applicable laws
            
            8. LIMITATION OF LIABILITY
            
            • The service is provided "as is" without warranties
            • We are not liable for any indirect or consequential damages
            • Our total liability shall not exceed the amount you paid us (if any)
            
            9. DATA AND PRIVACY
            
            • Your use of the service is also governed by our Privacy Policy
            • We collect and use data as described in the Privacy Policy
            • You are responsible for backing up your own data
            
            10. MODIFICATIONS
            
            We reserve the right to modify these terms at any time. Continued use of the service after changes constitutes acceptance of new terms.
            
            11. GOVERNING LAW
            
            These terms are governed by the laws of the jurisdiction in which our company is registered.
            
            12. CONTACT
            
            For questions about these terms, contact us at:
            legal@fieldsync.com
            
            By clicking "Create account," you acknowledge that you have read and agree to these Terms and Conditions.
        """.trimIndent()
    }

    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    private fun attemptSignUp() {
        // Verify checkboxes are still checked (extra validation)
        if (!binding.privacyPolicyCheckbox.isChecked || !binding.termsConditionsCheckbox.isChecked) {
            Toast.makeText(requireContext(), "You must agree to Privacy Policy and Terms & Conditions", Toast.LENGTH_SHORT).show()
            return
        }

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
                        "email" to email,
                        "agreedToPrivacyPolicy" to true,
                        "agreedToTerms" to true,
                        "agreementDate" to com.google.firebase.Timestamp.now()
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

        // Keep button state based on checkboxes if not loading
        if (!loading) {
            val privacyChecked = binding.privacyPolicyCheckbox.isChecked
            val termsChecked = binding.termsConditionsCheckbox.isChecked
            binding.signUpButton.isEnabled = privacyChecked && termsChecked
        } else {
            binding.signUpButton.isEnabled = false
        }

        binding.signUpUsername.isEnabled = !loading
        binding.signUpEmail.isEnabled = !loading
        binding.signUpPassword.isEnabled = !loading
        binding.signUpConfirmPassword.isEnabled = !loading
        binding.privacyPolicyCheckbox.isEnabled = !loading
        binding.termsConditionsCheckbox.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private enum class PolicyType {
        PRIVACY, TERMS
    }
}