package com.example.fieldsync

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fieldsync.databinding.FragmentCheckInBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

// MPAndroidChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

// ViewModel + model
import com.example.fieldsync.salesmock.CheckInSalesViewModel
import com.example.fieldsync.salesmock.SalesPoint

class CheckIn : Fragment() {

    private var _binding: FragmentCheckInBinding? = null
    private val binding get() = _binding!!

    private val salesVm: CheckInSalesViewModel by viewModels()

    // Firebase constants
    private companion object {
        const val TAG = "CheckIn"
        const val COLLECTION = "Visit_Check_In_Out"
        const val STORE_COLLECTION = "Store_Management"
        const val FIELD_STORE_ID = "StoreID"
        const val FIELD_STORE_NAME = "StoreName"
        const val FIELD_CHECK_IN = "CheckIn"
        const val FIELD_CHECK_OUT = "CheckOut"
        const val FIELD_VISIT_DURATION = "VisitDuration"
        const val FIELD_VISIT_ID = "VisitID"
        const val FIELD_STATUS = "Status"
        const val FIELD_LATITUDE = "Latitude"
        const val FIELD_LONGITUDE = "Longitude"

        // store management fields
        const val STORE_FIELD_STORE_ID = "StoreID"
        const val STORE_FIELD_STORE_NAME = "Store Name"

        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    private val prefs by lazy {
        requireContext().getSharedPreferences("visits", Context.MODE_PRIVATE)
    }

    private var currentVisitDocumentId: String? = null
    private var currentVisitId: Long? = null
    private var selectedStoreId: Long? = null
    private var selectedStoreName: String? = ""

    // Store list for dropdown
    private val storeList = mutableListOf<StoreItem>()
    private var storeAdapter: ArrayAdapter<String>? = null

    // Fused location provider for GPS
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    data class StoreItem(
        val storeId: Long,
        val storeName: String
    ) {
        override fun toString(): String = "$storeName (ID: $storeId)"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckInBinding.inflate(inflater, container, false)

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize coordinate labels
        setCoordLabels(null, null)

        // get store info from args (from StoreManagement)
        arguments?.let { args ->
            selectedStoreId = args.getLong("storeID", -1L).takeIf { it != -1L }
            selectedStoreName = args.getString("storeName")
        }

        restoreState()
        checkForActiveVisit()
        loadStoresFromFirebase()

        // Setup AutoCompleteTextView item click listener
        binding.checkInStoreEt.setOnItemClickListener { parent, _, position, _ ->
            val selectedText = parent.getItemAtPosition(position).toString()
            // Extract store ID from the selected text
            val storeItem = storeList.find { it.toString() == selectedText }
            storeItem?.let {
                selectedStoreId = it.storeId
                selectedStoreName = it.storeName
                Log.d(TAG, "Selected store: ${it.storeName} (ID: ${it.storeId})")
            }
        }

        binding.checkInBackBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Buttons
        binding.checkInCheckInBtn.setOnClickListener { performCheckIn() }
        binding.checkInCheckOutBtn.setOnClickListener { performCheckOut() }

        // Sales card wiring via ViewModel
        setupSalesChart(binding.salesChart)

        // Always collect — even if no storeId yet
        viewLifecycleOwner.lifecycleScope.launch {
            salesVm.series.collectLatest { points ->
                renderSales(binding.salesChart, points)
                binding.salesCard.visibility = if (points.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        // Optionally load initial data if store already known
        val storeIdForChart = VisitUtil.getCurrentStoreId(requireContext()) ?: selectedStoreId
        if (storeIdForChart != null) {
            salesVm.loadForStore(storeIdForChart)
        } else {
            binding.salesCard.visibility = View.GONE
        }

        return binding.root
    }

    private fun loadStoresFromFirebase() {
        Log.d(TAG, "Loading stores from Firebase...")

        Firebase.firestore.collection(STORE_COLLECTION)
            .get()
            .addOnSuccessListener { documents ->
                storeList.clear()

                for (document in documents) {
                    try {
                        val storeId = document.getLong(STORE_FIELD_STORE_ID)
                        val storeName = document.getString(STORE_FIELD_STORE_NAME)

                        if (storeId != null && !storeName.isNullOrEmpty()) {
                            storeList.add(StoreItem(storeId, storeName))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing store document", e)
                    }
                }

                // Sort by store name
                storeList.sortBy { it.storeName }

                // Create adapter with formatted strings
                val displayList = storeList.map { it.toString() }
                    storeAdapter = ArrayAdapter(
                        requireContext(),
                        R.layout.fragment_dropdown_item,   //Custom layout for dropdown
                        R.id.dropdownText,
                        displayList
                    )
                binding.checkInStoreEt.setAdapter(storeAdapter)

                Log.d(TAG, "Loaded ${storeList.size} stores")

                // If we have a pre-selected store, show it
                selectedStoreId?.let { storeId ->
                    val storeItem = storeList.find { it.storeId == storeId }
                    storeItem?.let {
                        binding.checkInStoreEt.setText(it.toString(), false)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading stores", e)
                Toast.makeText(requireContext(), "Failed to load stores: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Sales chart helpers
    private fun setupSalesChart(chart: LineChart) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.axisLeft.setDrawGridLines(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.setTouchEnabled(true)
        chart.setPinchZoom(false)
    }

    private fun renderSales(chart: LineChart, points: List<SalesPoint>) {
        if (points.isEmpty()) {
            chart.data = null
            chart.invalidate()
            return
        }

        val entries = points.mapIndexed { idx, p -> Entry(idx.toFloat(), p.units.toFloat()) }
        val set = LineDataSet(entries, "Sales").apply {
            setDrawCircles(false)
            lineWidth = 2f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        chart.data = LineData(set)

        val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
        val labels = points.map { sdf.format(it.date) }
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.labelCount = 6
        chart.invalidate()
    }

    private fun restoreState() {
        currentVisitDocumentId = prefs.getString("current_visit_doc_id", null)
        currentVisitId = prefs.getLong("current_visit_id", -1L).takeIf { it != -1L }

        // Pre-fill store if we have it
        selectedStoreId?.let { storeId ->
            selectedStoreName?.let { storeName ->
                binding.checkInStoreEt.setText("$storeName (ID: $storeId)", false)
            }
        }
    }

    private fun checkForActiveVisit() {
        currentVisitDocumentId?.let { docId ->
            Firebase.firestore.collection(COLLECTION)
                .document(docId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val status = document.getString(FIELD_STATUS)
                        if (status == "checked_in") {
                            val checkInTime = document.getTimestamp(FIELD_CHECK_IN)
                            val storeName = document.getString(FIELD_STORE_NAME) ?: ""
                            val storeId = document.getLong(FIELD_STORE_ID) ?: 0L
                            val visitId = document.getLong(FIELD_VISIT_ID) ?: 0L

                            binding.checkInStoreEt.setText("$storeName (ID: $storeId)", false)
                            updateUi(
                                checkedIn = true,
                                store = "$storeName (ID: $storeId)",
                                start = checkInTime?.toDate()?.time ?: 0L,
                                end = 0L
                            )
                            currentVisitId = visitId
                            selectedStoreId = storeId
                            selectedStoreName = storeName

                            // refresh chart once we confirm store context
                            salesVm.loadForStore(storeId)
                        }
                    } else {
                        clearLocalVisitState()
                    }
                }
                .addOnFailureListener {
                    clearLocalVisitState()
                }
        } ?: run {
            updateUi(checkedIn = false, store = "", start = 0L, end = 0L)
        }
    }

    // Helper to get GPS coordinates safely
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(callback: (Double?, Double?) -> Unit) {
        val context = requireContext()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            Toast.makeText(context, "Please grant location permission and try again.", Toast.LENGTH_SHORT).show()
            callback(null, null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    callback(location.latitude, location.longitude)
                    setCoordLabels(location.latitude, location.longitude)
                } else {
                    Toast.makeText(context, "Unable to retrieve location.", Toast.LENGTH_SHORT).show()
                    callback(null, null)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to get location: ${it.message}", Toast.LENGTH_SHORT).show()
                callback(null, null)
            }
    }

    private fun performCheckIn() {
        val storeIdText = binding.checkInStoreEt.text?.toString()?.trim().orEmpty()
        if (storeIdText.isEmpty()) {
            Toast.makeText(requireContext(), "Please select or enter a store.", Toast.LENGTH_SHORT).show()
            return
        }

        // Try to extract store ID from selected text or parse as number
        var storeId: Long? = null

        // Check if it's from dropdown selection (format: "Store Name (ID: 123)")
        if (storeIdText.contains("(ID:") && storeIdText.contains(")")) {
            val idPart = storeIdText.substringAfter("(ID:").substringBefore(")").trim()
            storeId = idPart.toLongOrNull()
        } else {
            // Try to parse as direct number input
            storeId = storeIdText.toLongOrNull()
        }

        if (storeId == null) {
            Toast.makeText(requireContext(), "Invalid store ID format.", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch GPS before check-in
        getCurrentLocation { lat, lon ->
            Firebase.firestore.collection(STORE_COLLECTION)
                .whereEqualTo(STORE_FIELD_STORE_ID, storeId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        Toast.makeText(requireContext(), "Store ID $storeId not found.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val storeDocument = querySnapshot.documents.first()
                    val storeName = storeDocument.getString(STORE_FIELD_STORE_NAME) ?: "Unknown Store"

                    val visitId = System.currentTimeMillis()
                    val checkInTime = Timestamp.now()

                    val visitData = hashMapOf(
                        FIELD_STORE_ID to storeId,
                        FIELD_STORE_NAME to storeName,
                        FIELD_CHECK_IN to checkInTime,
                        FIELD_CHECK_OUT to null,
                        FIELD_VISIT_DURATION to null,
                        FIELD_VISIT_ID to visitId,
                        FIELD_STATUS to "checked_in",
                        FIELD_LATITUDE to lat,
                        FIELD_LONGITUDE to lon
                    )

                    Firebase.firestore.collection(COLLECTION)
                        .add(visitData)
                        .addOnSuccessListener { documentReference ->
                            currentVisitDocumentId = documentReference.id
                            currentVisitId = visitId
                            selectedStoreId = storeId
                            selectedStoreName = storeName

                            prefs.edit()
                                .putString("current_visit_doc_id", currentVisitDocumentId)
                                .putLong("current_visit_id", visitId)
                                .putLong("current_store_id", storeId)
                                .putString("current_store_name", storeName)
                                .apply()

                            Toast.makeText(requireContext(), "Checked in to $storeName", Toast.LENGTH_SHORT).show()
                            updateUi(
                                checkedIn = true,
                                store = "$storeName (ID: $storeId)",
                                start = checkInTime.toDate().time,
                                end = 0L
                            )

                            // refresh chart with this store
                            salesVm.loadForStore(storeId)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Check-in failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error validating store: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun performCheckOut() {
        val docId = currentVisitDocumentId ?: run {
            Toast.makeText(requireContext(), "You're not checked in.", Toast.LENGTH_SHORT).show()
            return
        }

        val checkOutTime = Timestamp.now()

        // Fetch GPS for check-out
        getCurrentLocation { lat, lon ->
            Firebase.firestore.collection(COLLECTION)
                .document(docId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val checkInTime = document.getTimestamp(FIELD_CHECK_IN)
                        val storeName = document.getString(FIELD_STORE_NAME) ?: ""
                        val storeId = document.getLong(FIELD_STORE_ID) ?: 0L

                        if (checkInTime != null) {
                            val durationMs = checkOutTime.toDate().time - checkInTime.toDate().time
                            val durationMinutes = durationMs / (1000 * 60)

                            val updates = hashMapOf<String, Any>(
                                FIELD_CHECK_OUT to checkOutTime,
                                FIELD_VISIT_DURATION to durationMinutes,
                                FIELD_STATUS to "checked_out"
                            )

                            lat?.let { updates[FIELD_LATITUDE] = it }
                            lon?.let { updates[FIELD_LONGITUDE] = it }

                            Firebase.firestore.collection(COLLECTION)
                                .document(docId)
                                .update(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Checked out from $storeName", Toast.LENGTH_SHORT).show()
                                    updateUi(
                                        checkedIn = false,
                                        store = "$storeName (ID: $storeId)",
                                        start = checkInTime.toDate().time,
                                        end = checkOutTime.toDate().time
                                    )
                                    clearLocalVisitState()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Check-out failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error retrieving visits ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun clearLocalVisitState() {
        currentVisitDocumentId = null
        currentVisitId = null
        selectedStoreId = null
        selectedStoreName = null
        prefs.edit().clear().apply()
    }

    private fun updateUi(checkedIn: Boolean, store: String, start: Long, end: Long) = binding.apply {
        checkInCheckInBtn.isEnabled = !checkedIn
        checkInCheckOutBtn.isEnabled = checkedIn

        // Disable store selection when checked in
        checkInStoreEt.isEnabled = !checkedIn

        checkInStatusTv.text =
            if (checkedIn) "Status: Checked In @ ${store.ifBlank { "—" }}"
            else "Status: Not Checked In"

        checkInStartTv.text = if (start > 0) "Start: ${fmtTime(start)}" else "Start: —"
        checkInEndTv.text = if (end > 0) "End: ${fmtTime(end)}" else "End: —"

        val dur = if (start > 0 && end > 0) end - start else 0L
        checkInDurationTv.text =
            if (dur > 0) "Visit Duration: ${fmtDuration(dur)}" else "Visit Duration: —"
    }

    // coordinate labels on screen
    private fun setCoordLabels(lat: Double?, lon: Double?) = binding.apply {
        val fmt = { d: Double? -> if (d != null) String.format(Locale.US, "%.6f", d) else "—" }
        checkInLatTv.text = "Latitude: ${fmt(lat)}"
        checkInLonTv.text = "Longitude: ${fmt(lon)}"
    }

    private fun fmtTime(t: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.getDefault())
        return sdf.format(Date(t))
    }

    private fun fmtDuration(ms: Long): String {
        var secs = ms / 1000
        val hrs = secs / 3600
        secs %= 3600
        val mins = secs / 60
        secs %= 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}