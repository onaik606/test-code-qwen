package com.securemessenger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.securemessenger.databinding.ItemContactBinding
import com.securemessenger.network.ConnectedUser
import java.text.SimpleDateFormat
import java.util.*

class ContactsAdapter(
    private val onContactClick: OnContactClickListener
) : ListAdapter<ConnectedUser, ContactsAdapter.ContactViewHolder>(ContactDiffCallback()) {

    interface OnContactClickListener {
        fun onContactClick(contact: ConnectedUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(
        private val binding: ItemContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onContactClick.onContactClick(getItem(position))
                }
            }
        }

        fun bind(contact: ConnectedUser) {
            binding.apply {
                tvContactName.text = contact.displayName ?: "Unknown"
                tvConnectionStatus.text = if (contact.isConnected) "Connected" else "Pending"
                
                // Show last seen time if available
                contact.lastSeen?.let { timestamp ->
                    tvLastSeen.text = formatTimestamp(timestamp)
                } ?: run {
                    tvLastSeen.text = ""
                }
                
                // Visual indicator for connection status
                val statusColor = if (contact.isConnected) 
                    android.graphics.Color.GREEN else 
                    android.graphics.Color.ORANGE
                viewStatusIndicator.setBackgroundColor(statusColor)
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<ConnectedUser>() {
        override fun areItemsTheSame(oldItem: ConnectedUser, newItem: ConnectedUser): Boolean {
            return oldItem.phoneHash == newItem.phoneHash
        }

        override fun areContentsTheSame(oldItem: ConnectedUser, newItem: ConnectedUser): Boolean {
            return oldItem == newItem
        }
    }
}
