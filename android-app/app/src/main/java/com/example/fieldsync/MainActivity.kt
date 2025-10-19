package com.example.fieldsync

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fieldsync.databinding.ActivityMainBinding
import com.example.fieldsync.ui.login.LoginFragment
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Firebase manually (safe even if already initialized)
    FirebaseApp.initializeApp(this)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    // Check if user is already signed in
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
      // Go straight to MainMenu if already logged in
      SetActiveFragment(com.example.fieldsync.MainMenu())
    } else {
      // Otherwise, go to Login
      SetActiveFragment(LoginFragment())
    }
  }

  // Swaps Main with a new fragment
  fun SetActiveFragment(fragment: Fragment) {
    val transaction = supportFragmentManager.beginTransaction()
    transaction.replace(R.id.main, fragment)
      .addToBackStack(null)
      .commit()
  }
}
