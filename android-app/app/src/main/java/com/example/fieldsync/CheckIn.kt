package com.example.fieldsync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fieldsync.databinding.FragmentCheckInBinding
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp

class CheckIn : Fragment(R.layout.fragment_check_in) {

    private var _binding: FragmentCheckInBinding? = null
    private val binding get() = _binding!!

    // firebase constants
    private companion object {
        const val COLLECTION = "Visit_Check_In_Out"
        const val STORE_COLLECTION = "Store_Management"
        const val FIELD_STORE_ID = "StoreID"
        const val FIELD_STORE_NAME = "StoreName"
        const val FIELD_CHECK_IN = "CheckIn"
        const val FIELD_CHECK_OUT = "CheckOut"
        const val FIELD_VISIT_DURATION = "VisitDuration"
        const val FIELD_VISIT_ID = "VisitID"
        const val FIELD_STATUS = "Status" // "checked_in" or "checked_out"

        //store management fields
        const val STORE_FIELD_STORE_ID = "StoreID"
        const val STORE_FIELD_STORE_NAME = "Store Name"
    }

    private val prefs by lazy {
        requireContext().getSharedPreferences("visits", Context.MODE_PRIVATE)
    }

    // current visit data
    private var currentVisitDocumentId: String? = null
    private var currentVisitId: Long? = null
    private var selectedStoreId: Long? = null
    private var selectedStoreName: String? = ""



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckInBinding.inflate(inflater, container, false)

        // keep your insets handling
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // get store info from args, from StoreManagement
        arguments?.let { args ->
            selectedStoreId = args.getLong("storeID", -1L).takeIf { it != -1L }
            selectedStoreName = args.getString("storeName")
        }

        restoreState()
        checkForActiveVisit()

        // CHECK IN
        binding.checkInCheckInBtn.setOnClickListener {
            performCheckIn()
        }

        // CHECK OUT
        binding.checkInCheckOutBtn.setOnClickListener {
            performCheckOut()
        }

        binding.checkInBackBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun restoreState() {
        currentVisitDocumentId = prefs.getString("current_visit_doc_id", null)
        currentVisitId = prefs.getLong("current_visit_id", -1L).takeIf { it != -1L }

        // pre-fill store if we have it
        selectedStoreId?.let {
            binding.checkInStoreEt.setText(it.toString())
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
                            // we have an active visit
                            val checkInTime = document.getTimestamp(FIELD_CHECK_IN)
                            val storeName = document.getString(FIELD_STORE_NAME) ?: ""
                            val storeId = document.getLong(FIELD_STORE_ID) ?: 0L
                            val visitId = document.getLong(FIELD_VISIT_ID) ?: 0L

                            binding.checkInStoreEt.setText(storeId.toString())
                            updateUi(
                                checkedIn = true,
                                store = "$storeName (ID: $storeId)",
                                start = checkInTime?.toDate()?.time ?: 0L,
                                end = 0L
                            )
                        }
                    } else {
                        // doc doesn't exist, clear local state
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

    private fun performCheckIn() {
        val storeIdText = binding.checkInStoreEt.text?.toString()?.trim().orEmpty()
        if (storeIdText.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a store ID first.", Toast.LENGTH_SHORT).show()
            return
        }

        val storeId = storeIdText.toLongOrNull()
        if(storeId == null) {
            Toast.makeText(requireContext(), "Please enter a valid store ID.", Toast.LENGTH_SHORT).show()
            return
        }

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
                    FIELD_STATUS to "checked_in"
                )

                Firebase.firestore.collection(COLLECTION)
                    .add(visitData)
                    .addOnSuccessListener { documentReference ->
                        currentVisitDocumentId = documentReference.id
                        currentVisitId = visitId

                        prefs.edit()
                            .putString("current_visit_doc_id", currentVisitDocumentId)
                            .putLong("current_visit_id", visitId)
                            .putLong("current_store_id", storeId)
                            .putString("current_store_name", storeName)
                            .apply()

                        Toast.makeText(requireContext(), "Checked in successfully", Toast.LENGTH_SHORT).show()
                        updateUi(
                            checkedIn = true,
                            store = storeName,
                            start = checkInTime.toDate().time,
                            end = 0L
                        )
                    }
                    .addOnFailureListener {e ->
                        Toast.makeText(requireContext(), "Check-in failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error validating store: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun performCheckOut() {
        val docId = currentVisitDocumentId

        if (docId == null) {
            Toast.makeText(requireContext(), "You're not checked in.", Toast.LENGTH_SHORT).show()
            return
        }

        val checkOutTime = Timestamp.now()

        // first get current doc to calculate duration
        Firebase.firestore.collection(COLLECTION)
            .document(docId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val checkInTime = document.getTimestamp(FIELD_CHECK_IN)
                    val storeName = document.getString(FIELD_STORE_NAME) ?: ""
                    val storeId = document.getLong(FIELD_STORE_ID) ?: 0L
                    val visitId = document.getLong(FIELD_VISIT_ID) ?: 0L

                    if (checkInTime != null) {
                        val durationMs = checkOutTime.toDate().time - checkInTime.toDate().time
                        val durationMinutes = durationMs / (1000 * 60)

                        //update doc with check out info
                        val updates = hashMapOf<String, Any>(
                            FIELD_CHECK_OUT to checkOutTime,
                            FIELD_VISIT_DURATION to durationMinutes,
                            FIELD_STATUS to "checked_out"
                        )

                        Firebase.firestore.collection(COLLECTION)
                            .document(docId)
                            .update(updates)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Checked out successfully", Toast.LENGTH_SHORT).show()

                                updateUi(
                                    checkedIn = false,
                                    store = "$storeName (ID: $storeId)",
                                    start = checkInTime.toDate().time,
                                    end = checkOutTime.toDate().time
                                )
                                clearLocalVisitState()
                            }
                            .addOnFailureListener {e ->
                            Toast.makeText(requireContext(), "Check-out failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener {e ->
                Toast.makeText(requireContext(), "Error retrieving visits ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearLocalVisitState() {
        currentVisitDocumentId = null
        currentVisitId = null
        prefs.edit().clear().apply()
    }

        // CHECK IN
        /*binding.checkInCheckInBtn.setOnClickListener {
            val store = binding.checkInStoreEt.text?.toString()?.trim().orEmpty()
            if (store.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a store location first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val now = System.currentTimeMillis()
            // generate visitId
            val newVisitId = prefs.getInt(KEY_VISIT_ID, 0) + 1
            prefs.edit()
                .putBoolean(KEY_CHECKED_IN, true)
                .putString(KEY_STORE, store)
                .putLong(KEY_START_TIME, now)
                .putLong(KEY_END_TIME, 0L)
                .putInt(KEY_VISIT_ID, newVisitId)   // save visitId
                .apply()

            updateUi(checkedIn = true, store = store, start = now, end = 0L)
        }

        // CHECK OUT
        binding.checkInCheckOutBtn.setOnClickListener {
            val start = prefs.getLong(KEY_START_TIME, 0L)
            val store = prefs.getString(KEY_STORE, "") ?: ""
            if (start == 0L) {
                Toast.makeText(requireContext(), "You’re not checked in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val end = System.currentTimeMillis()
            val dur = end - start

            prefs.edit()
                .putBoolean(KEY_CHECKED_IN, false)
                .putLong(KEY_END_TIME, end)
                .apply()

            appendVisit(store, start, end, dur)
            updateUi(checkedIn = false, store = store, start = start, end = end)
        }

        binding.checkInBackBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }*/

    /*private fun restoreState() {
        val checkedIn = prefs.getBoolean(KEY_CHECKED_IN, false)
        val store = prefs.getString(KEY_STORE, "") ?: ""
        val start = prefs.getLong(KEY_START_TIME, 0L)
        val end = prefs.getLong(KEY_END_TIME, 0L)

        // prefill store box with last used value
        binding.checkInStoreEt.setText(store)
        updateUi(checkedIn, store, start, end)
    }*/

    private fun updateUi(checkedIn: Boolean, store: String, start: Long, end: Long) = binding.apply {
        // Buttons
        checkInCheckInBtn.isEnabled = !checkedIn
        checkInCheckOutBtn.isEnabled = checkedIn

        // Status + times
        checkInStatusTv.text =
            if (checkedIn) "Status: Checked In @ ${store.ifBlank { "—" }}"
            else "Status: Not Checked In"

        checkInStartTv.text = if (start > 0) "Start: ${fmtTime(start)}" else "Start: —"
        checkInEndTv.text   = if (end   > 0) "End: ${fmtTime(end)}"     else "End: —"

        val dur = if (start > 0 && end > 0) end - start else 0L
        checkInDurationTv.text =
            if (dur > 0) "Visit Duration: ${fmtDuration(dur)}" else "Visit Duration: —"
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

    /*private fun appendVisit(store: String, start: Long, end: Long, duration: Long) {
        // simple CSV log; will use in Visit History later
        val existing = prefs.getString(KEY_VISIT_LOG, "") ?: ""
        val line = "${store.ifBlank { "-" }},${fmtTime(start)},${fmtTime(end)},${fmtDuration(duration)}"
        val updated = if (existing.isBlank()) line else "$existing\n$line"
        prefs.edit().putString(KEY_VISIT_LOG, updated).apply()
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*companion object {
        private const val KEY_CHECKED_IN  = "checked_in"
        private const val KEY_STORE       = "store"
        private const val KEY_START_TIME  = "start_time"
        private const val KEY_END_TIME    = "end_time"
        private const val KEY_VISIT_LOG   = "visit_log"
        private const val KEY_VISIT_ID    = "visit_id"
    }*/
}

class VisitUtil {
    companion object {
        fun getCurrentVisitId(context: Context): Long? {
            val prefs = context.getSharedPreferences("visits", Context.MODE_PRIVATE)
            return prefs.getLong("current_visit_id", -1L).takeIf { it != -1L }
        }

        fun getCurrentStoreId(context: Context): Long? {
            val prefs = context.getSharedPreferences("visits", Context.MODE_PRIVATE)
            return prefs.getLong("current_store_id", -1L).takeIf { it != -1L }
        }
    }
}