package com.example.fieldsync

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fieldsync.databinding.FragmentVisitHistoryBinding
import com.example.fieldsync.databinding.FragmentVisitNotesBinding

class VisitNotes : Fragment() {

    private var _binding: FragmentVisitNotesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout using ViewBinding
        _binding = FragmentVisitNotesBinding.inflate(inflater, container, false)

        // This code controls what happens when submit button is pressed
        binding.visitNotesSubmitBtn.setOnClickListener {

        }

        return binding.root
    }

}