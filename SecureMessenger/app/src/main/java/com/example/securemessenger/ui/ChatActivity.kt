package com.example.securemessenger.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.securemessenger.SecureMessengerApp
import com.example.securemessenger.data.MessageRepository
import com.example.securemessenger.databinding.ActivityChatBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageRepository: MessageRepository
    private var currentUserId: String = ""
    private var otherUserId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        currentUserId = intent.getStringExtra("CURRENT_USER_ID") ?: return
        otherUserId = intent.getStringExtra("OTHER_USER_ID") ?: return
        
        val app = application as SecureMessengerApp
        messageRepository = MessageRepository(app.database.messageDao())
        
        setupRecyclerView()
        loadMessages()
        
        binding.btnSend.setOnClickListener {
            val content = binding.etMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                sendMessage(content)
                binding.etMessage.text.clear()
            }
        }
    }
    
    private fun setupRecyclerView() {
        // TODO: Setup RecyclerView with adapter for messages
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
    }
    
    private fun loadMessages() {
        lifecycleScope.launch {
            messageRepository.getMessagesBetweenUsers(currentUserId, otherUserId).collectLatest { messages ->
                // Update UI with messages
                // TODO: Update adapter with messages
            }
        }
    }
    
    private fun sendMessage(content: String) {
        lifecycleScope.launch {
            try {
                messageRepository.sendMessage(currentUserId, otherUserId, content)
                // Messages are stored locally and will be delivered when receiver is online
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
