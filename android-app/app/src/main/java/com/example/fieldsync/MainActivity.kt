package com.example.fieldsync

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fieldsync.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    SetActiveFragment(MainMenu())

  }

  // Swaps Main with a new fragment
  public fun SetActiveFragment(fragment: Fragment) {
    // lets you perform actions on fragments
    val transaction = supportFragmentManager.beginTransaction()

    // replaces main with new fragment
    transaction.replace(R.id.main, fragment)

    // Applies operation performed on fragment
    transaction.commit()
  }
}



