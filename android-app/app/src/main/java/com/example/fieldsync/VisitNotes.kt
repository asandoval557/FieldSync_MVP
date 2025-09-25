package com.example.fieldsync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fieldsync.databinding.FragmentVisitNotesBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.util.Date
import java.util.Locale


data class Note(
    var id: String? = null,
    var visitId: Long? = null, // changed from store to visitId
    var storeId: Long? = null, // added for additional linking
    var storeName: String? = null, // keep for display
    var body: String = "",
    var timestamp: Long = System.currentTimeMillis()
)

class VisitNotes : Fragment() {

    private var _binding: FragmentVisitNotesBinding? = null
    private val binding get() = _binding!!
    private val prefs by lazy {
        requireContext().getSharedPreferences("visits", Context.MODE_PRIVATE)
    }

    private val db = FirebaseFirestore.getInstance()

    // updated to use checkIn structure

    private val currentVisitId: Long?
        get() = VisitUtil.getCurrentVisitId(requireContext())

    private val currentStoreId: Long?
        get() = VisitUtil.getCurrentStoreId(requireContext())

    private val currentStoreName: String?
        get() = prefs.getString("current_store_name", null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitNotesBinding.inflate(inflater, container, false)

        if (currentStoreName.isNullOrEmpty() || currentVisitId == null) {
            Toast.makeText(requireContext(), "You must check in first.", Toast.LENGTH_SHORT).show()
            disableUi()
            return binding.root
        }

        // ADD NOTE
        binding.visitNotesSubmitBtn.setOnClickListener {
            val noteText = binding.visitNotesEditNoteTxt.text.toString().trim()
            if (noteText.isEmpty()) {
                Toast.makeText(requireContext(), "Note cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            db.collection("Visit_Notes")
                .get()
                .addOnSuccessListener { allNotesSnapshot ->
                    val noteId = (allNotesSnapshot.size() + 1).toString() // sequential numeric ID

                    val newNote = Note(
                        id = noteId,
                        visitId = currentVisitId,
                        storeId = currentStoreId,
                        storeName = currentStoreName,
                        body = noteText,
                        timestamp = System.currentTimeMillis()
                    )

                    db.collection("Visit_Notes")
                        .document(noteId)
                        .set(newNote)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Note added with ID $noteId", Toast.LENGTH_SHORT).show()
                            binding.visitNotesEditNoteTxt.text?.clear()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Error adding note: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error getting note count: ${e.message}", Toast.LENGTH_SHORT)
                    }
        }

                    /*val notesRef = db.collection("stores")
                .document(currentStore!!)
                .collection("notes")*/

                    /*notesRef.get().addOnSuccessListener { snapshot ->


                val newNote = Note(
                    id = nextId,
                    store = currentStore,
                    body = noteText
                )

                notesRef.document(nextId).set(newNote)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Note added with ID $nextId", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error adding note", Toast.LENGTH_SHORT).show()
                    }
            }*/



        // GET NOTES
        binding.visitNotesGetBtn.setOnClickListener {
            db.collection("Visit_Notes")
                .whereEqualTo("visitId", currentVisitId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val notes = snapshot.documents.mapNotNull { doc ->
                        try {
                            Note(
                                id = doc.getString("id"),
                                visitId = doc.getLong("visitId"),
                                storeId = doc.getLong("storeId"),
                                storeName = doc.getString("storeName"),
                                body = doc.getString("body") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (notes.isEmpty()) {
                        binding.visitNotesResultTxt.text = "No notes found for $currentStoreName"
                    } else {
                        binding.visitNotesResultTxt.text = notes.joinToString("\n\n") { note ->
                            val date = java.text.SimpleDateFormat(
                                "MMM d, yyyy h:mm a", Locale.getDefault()
                            ).format(Date(note.timestamp))

                            "Note ID: ${note.id}\nNote: ${note.body}\nCreated: $date"
                        }
                    }
                }
                .addOnFailureListener {
                    binding.visitNotesResultTxt.text = "Failed to load notes"
                }
        }


        // UPDATE NOTE
        binding.visitNotesUpdateBtn.setOnClickListener {
            val noteText = binding.visitNotesEditNoteTxt.text.toString().trim()
            val noteId = binding.visitNotesNoteIdTxt.text.toString().trim()

            if (noteText.isEmpty() || noteId.isEmpty()) {
                Toast.makeText(requireContext(), "Enter Note ID and text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /*if (!noteId.all { it.isDigit() } || noteId.toInt() <= 0) {
                Toast.makeText(requireContext(), "Note ID must be a positive number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }*/

            db.collection("Visit_Notes")
                .whereEqualTo("visitId", currentVisitId)
                .whereEqualTo("id", noteId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty()) {
                        Toast.makeText(requireContext(), "Note not found for this visit", Toast.LENGTH_SHORT).show()
                    } else {
                        val document = snapshot.documents.first()
                        document.reference.update(
                            mapOf(
                                "body" to noteText,
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Note $noteId updated!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to update note", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error finding note: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // DELETE NOTE
        binding.visitNotesDeleteBtn.setOnClickListener {
            val noteId = binding.visitNotesNoteIdTxt.text.toString().trim()

            if (noteId.isEmpty()) {
                Toast.makeText(requireContext(), "Enter Note ID to delete", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /*if (!noteId.all { it.isDigit() } || noteId.toInt() <= 0) {
                Toast.makeText(requireContext(), "Note ID must be a positive number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }*/

            db.collection("Visit_Notes")
                .whereEqualTo("visitId", currentVisitId)
                .whereEqualTo("id", noteId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty()) {
                        Toast.makeText(requireContext(), "Note $noteId not found for this visit", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val document = snapshot.documents.first()
                        document.reference.delete()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Note $noteId deleted!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to delete note", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error finding note", Toast.LENGTH_SHORT).show()
                }
        }


        // BACK BUTTON
        binding.visitNotesBackBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun disableUi() = binding.apply {
        visitNotesSubmitBtn.isEnabled = false
        visitNotesGetBtn.isEnabled = false
        visitNotesUpdateBtn.isEnabled = false
        visitNotesDeleteBtn.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
