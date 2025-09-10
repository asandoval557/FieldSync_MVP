package com.example.fieldsync

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fieldsync.databinding.FragmentPhotoCaptureBinding
import com.example.fieldsync.databinding.FragmentVisitNotesBinding

class PhotoCapture : Fragment() {

    private var _binding: FragmentPhotoCaptureBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPhotoCaptureBinding.inflate(inflater, container, false)

        // This code controls what happens when open camera button is pressed
        binding.photoCaptureOpenCameraBtn.setOnClickListener {

        }

        return binding.root
    }


}