package com.example.fieldsync

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fieldsync.databinding.FragmentPhotoCaptureBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PhotoCapture : Fragment() {

    private var _binding: FragmentPhotoCaptureBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext().getSharedPreferences("photos", Context.MODE_PRIVATE)
    }

    private var latestBitmap: Bitmap? = null

    // Simple MVP: capture a preview bitmap (no permissions needed)
    private val takePicturePreview = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            latestBitmap = bmp
            binding.photoPreviewIv.setImageBitmap(bmp)
            binding.photoStatusTv.text = "Preview ready"
            binding.photoSaveBtn.isEnabled = true
        } else {
            binding.photoStatusTv.text = "Capture canceled"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoCaptureBinding.inflate(inflater, container, false)

        // Avoid overlapping system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom); insets
        }

        restoreLastSavedPreview()

        // Open camera
        binding.photoCaptureOpenCameraBtn.setOnClickListener {
            binding.photoSaveBtn.isEnabled = false
            binding.photoStatusTv.text = "Opening camera…"
            takePicturePreview.launch(null)
        }

        // Save preview
        binding.photoSaveBtn.setOnClickListener {
            val bmp = latestBitmap
            if (bmp == null) {
                Toast.makeText(requireContext(), "No photo to save.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val file = saveToAppPictures(bmp)
            if (file != null) {
                prefs.edit().putString(KEY_LAST_PATH, file.absolutePath).apply()
                binding.photoStatusTv.text = "Saved: ${file.name}"
                Toast.makeText(requireContext(), "Saved to ${file.name}", Toast.LENGTH_SHORT).show()
            } else {
                binding.photoStatusTv.text = "Save failed"
                Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun saveToAppPictures(bmp: Bitmap): File? {
        return try {
            val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: requireContext().filesDir
            val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(dir, "FieldSync_$ts.jpg")
            FileOutputStream(file).use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun restoreLastSavedPreview() {
        val path = prefs.getString(KEY_LAST_PATH, null) ?: return
        val file = File(path)
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)?.let { bmp ->
                latestBitmap = bmp
                binding.photoPreviewIv.setImageBitmap(bmp)
                binding.photoStatusTv.text = "Last saved: ${file.name}"
                binding.photoSaveBtn.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_LAST_PATH = "last_photo_path"
    }
}
