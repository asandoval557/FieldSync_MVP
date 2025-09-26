package com.example.fieldsync

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.fieldsync.databinding.FragmentPhotoCaptureBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PhotoCapture : Fragment() {

    private var _binding: FragmentPhotoCaptureBinding? = null
    private val binding get() = _binding!!

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>

    private var tempPhotoUri: Uri? = null
    private var currentPhotoFile: File? = null

    companion object {
        private const val TAG = "PhotoCapture"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCameraPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                Log.d(TAG, "Camera permission result: $granted")
                if (granted) {
                    launchCamera()
                } else {
                    toast("Camera permission denied")
                }
            }

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                val file = currentPhotoFile
                Log.d(TAG, "Take picture result: success=$success, file=$file")
                if (success && file != null && file.exists()) {
                    onPhotoCaptured(file)
                } else {
                    toast("No photo captured")
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoCaptureBinding.inflate(inflater, container, false)

        binding.photoCaptureOpenCameraBtn.setOnClickListener {
            Log.d(TAG, "Camera button clicked")
            if (!isCheckedIn()) {
                toast("You're not checked into a store.")
                return@setOnClickListener
            }
            ensureCameraPermissionThenLaunch()
        }

        binding.photoCaptureBackBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun ensureCameraPermissionThenLaunch() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "Camera permission check: $hasPermission")

        if (hasPermission) {
            Log.d(TAG, "Permission granted, launching camera")
            launchCamera()
        } else {
            Log.d(TAG, "Permission not granted, requesting permission")
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        try {
            // Create app-specific storage directory for photos
            val picturesDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "visit_photos")
            if (!picturesDir.exists()) {
                picturesDir.mkdirs()
            }

            val visit = currentVisitContext()
            val fileName = if (visit != null) {
                "store_${visit.storeId}_visit_${visit.visitId}_${System.currentTimeMillis()}.jpg"
            } else {
                "photo_${System.currentTimeMillis()}.jpg"
            }

            val photoFile = File(picturesDir, fileName)
            currentPhotoFile = photoFile

            Log.d(TAG, "Photo file path: ${photoFile.absolutePath}")

            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )

            Log.d(TAG, "Photo URI: $photoUri")
            tempPhotoUri = photoUri
            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching camera", e)
            toast("Error launching camera: ${e.message}")
        }
    }

    private fun onPhotoCaptured(photoFile: File) {
        Log.d(TAG, "Photo captured, saving metadata to Firestore")
        savePhotoMetadataToFirestore(photoFile)
    }

    private fun savePhotoMetadataToFirestore(photoFile: File) {
        val visit = currentVisitContext()
        if (visit == null) {
            Log.e(TAG, "Missing visit context")
            toast("Missing visit context. Please check in again.")
            return
        }

        Log.d(TAG, "Visit context: StoreID=${visit.storeId}, VisitID=${visit.visitId}")
        Log.d(TAG, "Photo file: ${photoFile.absolutePath}, size: ${photoFile.length()} bytes")

        binding.photoCaptureOpenCameraBtn.text = "Saving..."
        binding.photoCaptureOpenCameraBtn.isEnabled = false

        val doc = hashMapOf(
            "StoreID" to visit.storeId,
            "StoreName" to visit.storeName,
            "VisitID" to visit.visitId,
            "PhotoPath" to photoFile.absolutePath,  // Local file path instead of URL
            "PhotoName" to photoFile.name,
            "PhotoSize" to photoFile.length(),      // File size in bytes
            "Timestamp" to FieldValue.serverTimestamp(),
            "CheckInDocumentId" to visit.checkInDocId
        )

        Firebase.firestore.collection("Visit_Photos")
            .add(doc)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Metadata saved with ID: ${documentReference.id}")
                binding.photoCaptureOpenCameraBtn.text = "Take Photo"
                binding.photoCaptureOpenCameraBtn.isEnabled = true
                toast("Photo saved successfully! (Local storage)")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save metadata", e)
                binding.photoCaptureOpenCameraBtn.text = "Take Photo"
                binding.photoCaptureOpenCameraBtn.isEnabled = true
                toast("Failed to save photo metadata: ${e.message}")
            }
    }

    private data class VisitCtx(
        val checkInDocId: String,
        val storeId: Long,
        val storeName: String,
        val visitId: Long
    )

    private fun currentVisitContext(): VisitCtx? {
        val p = prefs()
        val docId = p.getString("current_visit_doc_id", null)
        val storeId = p.getLong("current_store_id", -1L).takeIf { it != -1L }
        val storeName = p.getString("current_store_name", "")
        val visitId = p.getLong("current_visit_id", -1L).takeIf { it != -1L }

        Log.d(TAG, "Visit context - docId: $docId, storeId: $storeId, storeName: $storeName, visitId: $visitId")

        return if (docId != null && storeId != null && visitId != null) {
            VisitCtx(docId, storeId, storeName ?: "", visitId)
        } else null
    }

    private fun isCheckedIn(): Boolean {
        val checkedIn = prefs().getString("current_visit_doc_id", null) != null
        Log.d(TAG, "Is checked in: $checkedIn")
        return checkedIn
    }

    private fun prefs() =
        requireContext().getSharedPreferences("visits", 0)

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}