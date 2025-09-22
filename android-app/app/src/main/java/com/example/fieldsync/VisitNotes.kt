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

    private val prefs by lazy {
        requireContext().getSharedPreferences("visits", android.content.Context.MODE_PRIVATE)
    }

    private val visitId: Int
        get() = prefs.getInt("visit_id", -1)  // -1 means no active visit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitNotesBinding.inflate(inflater, container, false)

        // CREATE (POST)
        binding.visitNotesSubmitBtn.setOnClickListener {
            val noteText = binding.visitNotesEditNoteTxt.text.toString()
            if (noteText.isBlank()) {
                Toast.makeText(requireContext(), "Note cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val note = Note(visitId = visitId, body = noteText)

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
                            val notes = response.body()?.joinToString("\n\n") { note ->
                                " ID: ${note.id}\n ${note.body}"
                            } ?: "No notes found"
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
            val noteIdText = binding.visitNotesNoteIdTxt.text.toString()

            if (noteText.isBlank() || noteIdText.isBlank()) {
                Toast.makeText(requireContext(), "Enter Note ID and text to update", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val noteId = noteIdText.toInt()
            val updatedNote = Note(visitId = visitId, body = noteText)

            RetrofitClient.instance.updateNote(visitId, noteId, updatedNote)
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
            val noteIdText = binding.visitNotesNoteIdTxt.text.toString()
            if (noteIdText.isBlank()) {
                Toast.makeText(requireContext(), "Enter Note ID to delete", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val noteId = noteIdText.toInt()

            RetrofitClient.instance.deleteNote(visitId, noteId)
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

        binding.visitNotesBackBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
