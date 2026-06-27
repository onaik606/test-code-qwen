package com.example.securemessenger.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.securemessenger.SecureMessengerApp
import com.example.securemessenger.data.ConnectionRepository
import com.example.securemessenger.data.ConnectionStatus
import com.example.securemessenger.databinding.ActivityChatListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChatListBinding
    private lateinit var connectionRepository: ConnectionRepository
    private var currentUserId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        currentUserId = intent.getStringExtra("USER_ID") ?: return
        
        val app = application as SecureMessengerApp
        connectionRepository = ConnectionRepository(app.database.connectionDao())
        
        setupRecyclerView()
        loadConnections()
        
        binding.fabAddConnection.setOnClickListener {
            showAddConnectionDialog()
        }
    }
    
    private fun setupRecyclerView() {
        // TODO: Setup RecyclerView with adapter for connections
    }
    
    private fun loadConnections() {
        lifecycleScope.launch {
            connectionRepository.getConnectionsForUser(currentUserId).collectLatest { connections ->
                // Update UI with connections
                // Only show ACCEPTED connections for chatting
                val acceptedConnections = connections.filter { it.status == ConnectionStatus.ACCEPTED }
                // TODO: Update adapter with acceptedConnections
            }
        }
    }
    
    private fun showAddConnectionDialog() {
        // TODO: Show dialog to enter other user's phone number
        Toast.makeText(this, "Enter phone number to connect", Toast.LENGTH_SHORT).show()
    }
    
    private fun openChat(connectionId: String, otherUserId: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("CURRENT_USER_ID", currentUserId)
        intent.putExtra("OTHER_USER_ID", otherUserId)
        intent.putExtra("CONNECTION_ID", connectionId)
        startActivity(intent)
    }
}
