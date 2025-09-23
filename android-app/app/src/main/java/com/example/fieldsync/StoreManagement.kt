package com.example.fieldsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fieldsync.databinding.FragmentStoreManagementBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
            adapter = StoreAdapter(emptyList())
        }

        binding.storeManagementBackBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        fetchStores()
        return binding.root
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
    private class StoreAdapter(private var items: List<StoreItem>) :
        RecyclerView.Adapter<StoreAdapter.VH>() {

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(android.R.id.text1)
            val subtitle: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            val idPrefix = item.storeId?.let { "ID $it – " } ?: ""
            holder.title.text = idPrefix + item.title
            holder.subtitle.text = item.subtitle
        }

        override fun getItemCount(): Int = items.size

        fun update(newItems: List<StoreItem>) {
            items = newItems
            notifyDataSetChanged()
        }
    }
}
