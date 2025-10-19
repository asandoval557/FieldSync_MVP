package com.example.fieldsync

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fieldsync.databinding.FragmentStoreManagementBinding
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.widget.EditText
import android.widget.ImageButton
import android.app.AlertDialog
import java.io.File

class StoreManagement : Fragment(R.layout.fragment_store_management)  {

    private var _binding: FragmentStoreManagementBinding? = null
    private val binding get() = _binding!!

    private companion object {
        const val COLLECTION       = "Store_Management"
        const val FIELD_STORE_NAME = "Store Name"
        const val FIELD_ADDRESS    = "Address"
        const val FIELD_CITY       = "City"
        const val FIELD_STATE      = "State"
        const val FIELD_STORE_ID   = "StoreID"
    }

    // UI item (no VisitLocation)
    data class StoreItem(
        val documentId: String,    // Firestore doc id for deletion
        val storeId: Long?,        // may be null if missing
        val title: String,         // Store Name
        val subtitle: String       // "Address, City, State"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreManagementBinding.inflate(inflater, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom); insets
        }

        binding.visitRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = StoreAdapter(emptyList(),
                onDeleteClick = { store -> confirmDeleteStore(store) }
            )
        }

        binding.storeManagementBackBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // add store button
        binding.storeManagementAddBtn.setOnClickListener {
            showAddStoreDialog()
        }

        fetchStores()
        return binding.root
    }


    private fun showAddStoreDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_store, null)

        val storeNameInput = dialogView.findViewById<EditText>(R.id.dialog_store_name_input)
        val addressInput = dialogView.findViewById<EditText>(R.id.dialog_address_input)
        val cityInput = dialogView.findViewById<EditText>(R.id.dialog_city_input)
        val stateInput = dialogView.findViewById<EditText>(R.id.dialog_state_input)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Store")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val storeName = storeNameInput.text.toString().trim()
                val address = addressInput.text.toString().trim()
                val city = cityInput.text.toString().trim()
                val state = stateInput.text.toString().trim()

                if (storeName.isEmpty() || address.isEmpty() || city.isEmpty() || state.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "All fields are required",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                generateNextStoreIdAndAdd(storeName, address, city, state)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun generateNextStoreIdAndAdd(
        storeName: String,
        address: String,
        city: String,
        state: String
    ) {
        Firebase.firestore.collection(COLLECTION)
            .orderBy(FIELD_STORE_ID, com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val nextStoreId = if (snapshot.isEmpty) {
                    1L //start from 1 if no store exists
                } else {
                    val lastStoreId = snapshot.documents[0].getLong(FIELD_STORE_ID) ?: 0L
                    lastStoreId + 1
                }
                addStoreToFirestore(nextStoreId, storeName, address, city, state)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Error generating Store ID: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addStoreToFirestore(
        storeId: Long,
        storeName: String,
        address: String,
        city: String,
        state: String
    ) {
        // check if store id already exists
        Firebase.firestore.collection(COLLECTION)
            .whereEqualTo(FIELD_STORE_ID, storeId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    Toast.makeText(
                        requireContext(),
                        "Store ID $storeId already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                val storeData = hashMapOf(
                    FIELD_STORE_ID to storeId,
                    FIELD_STORE_NAME to storeName,
                    FIELD_ADDRESS to address.ifEmpty { null },
                    FIELD_CITY to city.ifEmpty { null },
                    FIELD_STATE to state.ifEmpty { null }
                )

                Firebase.firestore.collection(COLLECTION)
                    .add(storeData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Store added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchStores()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to add store ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Error checking store ID: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun confirmDeleteStore(store: StoreItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Store")
            .setMessage("Are you sure you want to delete this store ?")
            .setPositiveButton("Yes") { _, _ ->
                deleteStoreFromFirestore(store)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteStoreFromFirestore(store: StoreItem) {
        Firebase.firestore.collection(COLLECTION)
            .document(store.documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Store deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()
                fetchStores()
            }
            .addOnFailureListener { e ->
            Toast.makeText(
                requireContext(),
                "Failed to delete store ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun fetchStores() {
        Firebase.firestore.collection(COLLECTION)
            .get()
            .addOnSuccessListener { snap ->
                val items = snap.documents.mapNotNull { doc ->
                    val name = doc.getString(FIELD_STORE_NAME)?.trim().orEmpty()
                    if (name.isEmpty()) return@mapNotNull null

                    // Parse StoreID robustly (Number or String)
                    val storeId: Long? = doc.getLong(FIELD_STORE_ID)
                        ?: (doc.getDouble(FIELD_STORE_ID)?.toLong())
                        ?: (doc.getString(FIELD_STORE_ID)?.toLongOrNull())

                    val addr  = doc.getString(FIELD_ADDRESS)?.trim()
                    val city  = doc.getString(FIELD_CITY)?.trim()
                    val state = doc.getString(FIELD_STATE)?.trim()
                    val subtitle = listOfNotNull(addr, city, state)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")

                    StoreItem(
                        documentId = doc.id,
                        storeId = storeId,
                        title = name,
                        subtitle = subtitle
                    )
                }

                (binding.visitRecyclerView.adapter as StoreAdapter).update(items)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load stores: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Minimal inline adapter
    private class StoreAdapter(
        private var items: List<StoreItem>,
        private val onDeleteClick: (StoreItem) -> Unit) :
        RecyclerView.Adapter<StoreAdapter.VH>() {


        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val deleteBtn: ImageButton = view.findViewById(R.id.store_delete_btn)
            val image: ImageView = view.findViewById(R.id.storeImage)
            val title: TextView = view.findViewById(R.id.item_storeManagement_titleTxt)
            val subtitle: TextView = view.findViewById(R.id.item_storeManagement_subtitleTxt)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_store_management, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            val idPrefix = item.storeId?.let { "ID $it – " } ?: ""
            holder.title.text = idPrefix + item.title
            holder.subtitle.text = item.subtitle
            holder.deleteBtn.setOnClickListener {
                onDeleteClick(item)
            }

            // Binds latest photo holder.image
            loadRecentPhoto(item, holder.image, holder.image.context)
        }

        override fun getItemCount(): Int = items.size

        fun update(newItems: List<StoreItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        private fun loadRecentPhoto(item: StoreManagement.StoreItem, imageView: ImageView, context: Context) {
            Firebase.firestore.collection("Visit_Photos")
                .whereEqualTo("StoreID", item.storeId)
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.first()
                        val photoPath = doc.getString("PhotoPath")
                        if (photoPath != null) {
                            displayLocalPhoto(photoPath, imageView, context)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("PhotoFetch", "Error fetching photo metadata", e)
                }

        }

        fun displayLocalPhoto(path: String, imageView: ImageView, context: Context) {
            val file = File(path)
            if (file.exists()) {
                Glide.with(context)
                    .load(file)
                    .into(imageView)
            } else {
                Toast.makeText(context, "Could not find photos!", Toast.LENGTH_SHORT).show()
                Log.e("PhotoDisplay", "File not found: $path")
            }
        }

    }

}
