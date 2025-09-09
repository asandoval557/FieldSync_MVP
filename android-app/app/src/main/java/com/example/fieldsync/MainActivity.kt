package com.example.fieldsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        MainMenuScreen()
      }
    }
  }
}

@Composable
fun MainMenuScreen() {
  var selectedScreen by remember { mutableStateOf("mainMenu") }

  when (selectedScreen) {
    "mainMenu" -> Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text("Welcome to FieldSync MVP", style = MaterialTheme.typography.headlineMedium)

      Button(onClick = { selectedScreen = "jobList" }) {
        Text("Job List")
      }

      Button(onClick = { selectedScreen = "checkIn" }) {
        Text("GPS Check-In")
      }

      Button(onClick = { selectedScreen = "photoCapture" }) {
        Text("Photo Capture")
      }

      Button(onClick = { selectedScreen = "visitHistory" }) {
        Text("Visit History")
      }
    }

    else -> SimpleScreen(title = selectedScreen) {
      selectedScreen = "mainMenu"
    }
  }
}

@Composable
fun SimpleScreen(title: String, onBack: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text("$title Screen", style = MaterialTheme.typography.headlineMedium)
    Button(onClick = onBack) {
      Text("Back to Menu")
    }
  }
}

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
  MaterialTheme {
    MainMenuScreen()
  }
}