package com.example.fieldsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fieldsync.api.Note
import com.example.fieldsync.api.RetrofitClient
import com.example.fieldsync.databinding.FragmentVisitNotesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VisitNotes : Fragment() {

    private var _binding: FragmentVisitNotesBinding? = null
    private val binding get() = _binding!!

    // Hardcoded for prototype; later replace with CheckIn ID
    private val visitId = 1
    private val hardcodedNoteId = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVisitNotesBinding.inflate(inflater, container, false)

        // CREATE (POST)
        binding.visitNotesSubmitBtn.setOnClickListener {
            val noteText = binding.visitNotesEditNoteTxt.text.toString()
            if (noteText.isBlank()) {
                Toast.makeText(requireContext(), "Note cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val note = Note(visit_id = visitId, content = noteText)

            RetrofitClient.instance.addNote(visitId, note)
                .enqueue(object : Callback<Note> {
                    override fun onResponse(call: Call<Note>, response: Response<Note>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Note added!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to add note", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Note>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // READ (GET)
        binding.visitNotesGetBtn.setOnClickListener {
            RetrofitClient.instance.getNotes(visitId)
                .enqueue(object : Callback<List<Note>> {
                    override fun onResponse(call: Call<List<Note>>, response: Response<List<Note>>) {
                        if (response.isSuccessful) {
                            val notes = response.body()?.joinToString("\n") { "ID: ${it.id}, ${it.content}" }
                                ?: "No notes found"
                            binding.visitNotesResultTxt.text = notes
                        } else {
                            binding.visitNotesResultTxt.text = "Failed to load notes"
                        }
                    }

                    override fun onFailure(call: Call<List<Note>>, t: Throwable) {
                        binding.visitNotesResultTxt.text = "Error: ${t.message}"
                    }
                })
        }

        // UPDATE (PUT)
        binding.visitNotesUpdateBtn.setOnClickListener {
            val noteText = binding.visitNotesEditNoteTxt.text.toString()
            if (noteText.isBlank()) {
                Toast.makeText(requireContext(), "Note cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedNote = Note(visit_id = visitId, content = noteText)

            RetrofitClient.instance.updateNote(visitId, hardcodedNoteId, updatedNote)
                .enqueue(object : Callback<Note> {
                    override fun onResponse(call: Call<Note>, response: Response<Note>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Note updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to update note", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Note>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // DELETE (DELETE)
        binding.visitNotesDeleteBtn.setOnClickListener {
            RetrofitClient.instance.deleteNote(visitId, hardcodedNoteId)
                .enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Note deleted!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete note", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
