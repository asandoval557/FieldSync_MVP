package com.example.fieldsync.StoreManagement

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fieldsync.R
import com.example.fieldsync.databinding.ActivityStoreManagementBinding

class StoreManagement : AppCompatActivity() {

    private lateinit var binding: ActivityStoreManagementBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        binding = ActivityStoreManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val visitList = listOf(
            VisitLocation("Best Buy - Downtown", "123 Main St," +
                    " San Antonio, TX", " 9:00 AM - 10:30 AM", "Upcoming"),
            VisitLocation("Target - Northside", "456 Oak Ave, San Antonio, TX",
                "11:00 AM - 12:30 PM", "Upcoming"),
            VisitLocation("Walmart - Southpark", "789 Elm St, San Antonio, TX",
                "2:00 PM - 3:30 PM", "Upcoming"),
        )

        binding.visitRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@StoreManagement)
            adapter = VisitLocationAdapter(visitList)
        }

    }
}