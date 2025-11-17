package com.example.fieldsync

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fieldsync.databinding.FragmentVisitNotesBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*


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

    private val selectedNotes = mutableSetOf<Note>()
    private var savedNotes: List<Note> = emptyList()
    private var isSelectionMode = false


    private val db = FirebaseFirestore.getInstance()

    // updated to use checkIn structure

    private val currentVisitId: Long?
        get() = VisitUtil.getCurrentVisitId(requireContext())

    private val currentStoreId: Long?
        get() = VisitUtil.getCurrentStoreId(requireContext())

    private val currentStoreName: String?
        get() = prefs.getString("current_store_name", null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.visitNotesToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_select_mode -> {
                    isSelectionMode = !isSelectionMode
                    selectedNotes.clear()
                    updateToolbarMenu()
                    getNotes()
                    true
                }

                R.id.action_delete_selected -> {
                    deleteSelectedNotes()
                    true
                }
                R.id.action_add_note -> {
                    if (!isSelectionMode) showAddNoteDialog()
                    true
                }
                else -> false


            }
        }

        getNotes()

        // Allows recycler view to be swiped
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = savedNotes[position]
                    deleteNote(note)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.visitNotesNoteList)


        // Back Button
        binding.visitNotesToolbar.apply {
            navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back)
            setNavigationContentDescription("Back")
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }




    }


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

        return binding.root
    }

    private fun editNote(noteId: String?, noteText: String) {

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
                            getNotes()
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

    private fun addNote(noteText: String) {

        // Count only notes for this store to make sequential IDs per store
        db.collection("Visit_Notes")
            .whereEqualTo("storeId", currentStoreId)
            .get()
            .addOnSuccessListener { allNotesSnapshot ->
                val noteId = UUID.randomUUID().toString()

                val newNote = Note(
                    id = noteId,
                    visitId = currentVisitId,
                    storeId = currentStoreId,
                    storeName = currentStoreName,
                    body = noteText,
                    timestamp = System.currentTimeMillis()
                )

                // Namespaced doc id to avoid clashes across stores
                db.collection("Visit_Notes")
                    .document("${currentStoreId}_${noteId}")
                    .set(newNote)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Note added successfully", Toast.LENGTH_SHORT).show()
                        getNotes()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error adding note: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error getting note count: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getNotes() {
        db.collection("Visit_Notes")
            .whereEqualTo("storeId", currentStoreId)                      // ← filter by StoreID
            .orderBy("timestamp", Query.Direction.DESCENDING)
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
                    } catch (_: Exception) {
                        null
                    }
                }
                savedNotes = notes
                if (notes.isEmpty()) {
                  Toast.makeText(context,"No notes found for $currentStoreName", Toast.LENGTH_SHORT).show()
                }
                else {
                    binding.visitNotesNoteList.layoutManager = LinearLayoutManager(requireContext())
                    binding.visitNotesNoteList.adapter = NotesAdapter(view, notes,
                        onNoteClick = { note ->
                            if (isSelectionMode) {
                                toggleSelection(note)
                            } else {
                                showEditNoteDialog(note)
                            }
                        },
                        onNoteLongClick = { note ->

                        },
                        isSelected = { note -> selectedNotes.contains(note) }

                    )

                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context,"Failed to load notes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun disableUi() = binding.apply {
        //visitNotesUpdateBtn.isEnabled = false
     //   visitNotesDeleteBtn.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class NotesAdapter(
        view: View?,
        val items: List<Note>,
        private val onNoteClick: (Note) -> Unit,
        private val onNoteLongClick: (Note) -> Unit,
        private val isSelected: (Note) -> Boolean
    ) : RecyclerView.Adapter<NotesAdapter.VH>() {

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val noteDate: TextView = view.findViewById(R.id.visitNotes_noteDate)
            val noteBody: TextView = view.findViewById(R.id.visitNotes_noteBody)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_visit_note, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            val date = SimpleDateFormat(
                "MMM d, yyyy h:mm a", Locale.getDefault()
            ).format(Date(item.timestamp))
            holder.noteDate.text = "Created: $date"
            holder.noteBody.text = item.body

            // Lets you tap on note to edit it
            holder.itemView.setOnClickListener {
                onNoteClick(item)
            }

            // Highlight selected notes
            holder.itemView.setBackgroundColor(
                if (isSelected(item)) Color.LTGRAY else Color.TRANSPARENT
            )

        }


        override fun getItemCount(): Int = items.size
    }

    private fun showAddNoteDialog() {

        // Used to match edit text padding
        val titleView = TextView(requireContext()).apply {
            text = "New Note"
            setPadding(24, 24, 24, 0)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
        }

        val input = EditText(requireContext()).apply {
            hint = "Enter note"
            minLines = 1
            gravity = Gravity.BOTTOM
            setPadding(24, 24, 24, 24)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setCustomTitle(titleView)
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val noteText = input.text.toString().trim()
                if (noteText.isNotEmpty()) {
                    addNote(noteText)
                } else {
                    Toast.makeText(requireContext(), "Note cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#E0E0E0")))
    }

    private fun showEditNoteDialog(note: Note) {
        val input = EditText(requireContext()).apply {
            setText(note.body)
            hint = "Enter note"
            minLines = 1
            gravity = Gravity.BOTTOM
            setPadding(24, 24, 24, 24)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Note")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val updatedText = input.text.toString().trim()
                if (updatedText.isNotEmpty()) {
                    editNote(note.id, updatedText)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#E0E0E0")))
    }

    private fun toggleSelection(note: Note) {
        if (selectedNotes.contains(note)) {
            selectedNotes.remove(note)
        } else {
            selectedNotes.add(note)
        }
        getNotes()
    }

    private fun deleteSelectedNotes() {
        if (selectedNotes.isEmpty()) {
            Toast.makeText(requireContext(), "No notes selected", Toast.LENGTH_SHORT).show()
            return
        }

        var failedCount = 0

        selectedNotes.forEach { note ->
            db.collection("Visit_Notes")
                .whereEqualTo("id", note.id)
                .get()
                .addOnSuccessListener { snapshot ->
                    val docRef = snapshot.documents.firstOrNull()?.reference
                    if (docRef != null) {
                        docRef.delete()
                            .addOnFailureListener {
                                failedCount++
                            }
                    } else {
                        failedCount++
                    }
                }
                .addOnFailureListener {
                    failedCount++
                }
        }

        selectedNotes.clear()
        isSelectionMode = false
        updateToolbarMenu()

        // Delay refresh slightly to allow deletions to complete
        binding.visitNotesNoteList.postDelayed({
            getNotes()
        }, 500)
    }

    private fun deleteNote(note: Note) {
        db.collection("Visit_Notes")
            .whereEqualTo("visitId", currentVisitId)
            .whereEqualTo("id", note.id)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.firstOrNull()?.reference?.delete()?.addOnSuccessListener {
                    getNotes()
                }
            }
    }

    private fun updateToolbarMenu() {
        val menu = binding.visitNotesToolbar.menu
        menu.findItem(R.id.action_delete_selected)?.isVisible = isSelectionMode
    }

}
