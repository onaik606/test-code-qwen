package com.securemessenger.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.securemessenger.R
import com.securemessenger.adapter.ContactsAdapter
import com.securemessenger.databinding.ActivityMainBinding
import com.securemessenger.model.AppDatabase
import com.securemessenger.model.Contact
import com.securemessenger.network.ApiClient
import com.securemessenger.network.ConnectedUser
import com.securemessenger.security.MessageEncryptionManager
import com.securemessenger.service.MessagePollingService
import com.securemessenger.utils.SecurityUtils
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ContactsAdapter.OnContactClickListener {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var contactsAdapter: ContactsAdapter
    private var connectedContacts = mutableListOf<ConnectedUser>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        if (!SecurityUtils.isLoggedIn()) {
            navigateToLogin()
            return
        }
        
        setupUI()
        loadConnectedContacts()
        
        // Start message polling service
        startMessagePolling()
    }
    
    private fun setupUI() {
        // Setup RecyclerView for contacts
        contactsAdapter = ContactsAdapter(this)
        binding.recyclerViewContacts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewContacts.adapter = contactsAdapter
        
        // Add new contact button
        binding.fabAddContact.setOnClickListener {
            showAddContactDialog()
        }
        
        // Logout button
        binding.btnLogout.setOnClickListener {
            SecurityUtils.clearSession()
            navigateToLogin()
        }
        
        // Refresh contacts
        binding.swipeRefresh.setOnRefreshListener {
            loadConnectedContacts()
            binding.swipeRefresh.isRefreshing = false
        }
    }
    
    private fun loadConnectedContacts() {
        lifecycleScope.launch {
            try {
                val userHash = SecurityUtils.getPhoneHash() ?: return@launch
                
                // Try to fetch from server
                val response = ApiClient.apiService.getConnectedUsers(userHash)
                
                if (response.isSuccessful && response.body() != null) {
                    connectedContacts.clear()
                    connectedContacts.addAll(response.body()!!.users)
                    contactsAdapter.submitList(connectedContacts)
                    
                    // Update local database
                    updateLocalContacts(response.body()!!.users)
                } else {
                    // Load from local database if server fails
                    loadLocalContacts()
                }
            } catch (e: Exception) {
                // Load from local database on error
                loadLocalContacts()
            }
        }
    }
    
    private suspend fun updateLocalContacts(users: List<ConnectedUser>) {
        val db = AppDatabase.getDatabase(this@MainActivity)
        val contactDao = db.contactDao()
        
        users.forEach { user ->
            val contact = Contact(
                phoneHash = user.phoneHash,
                displayName = user.displayName ?: "Unknown",
                isConnected = user.isConnected,
                lastMessageTime = user.lastSeen
            )
            contactDao.insertContact(contact)
        }
    }
    
    private fun loadLocalContacts() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@MainActivity)
            db.contactDao().getConnectedContacts().collect { contacts ->
                contactsAdapter.submitList(contacts.map { 
                    ConnectedUser(it.phoneHash, it.displayName, it.isConnected, it.lastMessageTime) 
                })
            }
        }
    }
    
    private fun showAddContactDialog() {
        // Show dialog to enter phone number of contact to connect with
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Add Contact")
            .setMessage("Enter phone number to connect with")
            .setView(R.layout.dialog_add_contact)
            .setPositiveButton("Connect") { _, _ ->
                // Will be handled in showAlertDialog
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
        
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val editText = dialog.findViewById<EditText>(R.id.etContactPhone)
            val phoneNumber = editText?.text.toString().trim()
            
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            sendConnectionRequest(phoneNumber)
            dialog.dismiss()
        }
    }
    
    private fun sendConnectionRequest(phoneNumber: String) {
        lifecycleScope.launch {
            try {
                val userHash = SecurityUtils.getPhoneHash() ?: return@launch
                val contactHash = SecurityUtils.hashPhoneNumber(phoneNumber)
                
                val request = com.securemessenger.network.ConnectionRequest(
                    fromUserHash = userHash,
                    toUserHash = contactHash
                )
                
                val response = ApiClient.apiService.sendConnectionRequest(request)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Connection request sent", Toast.LENGTH_SHORT).show()
                    
                    // Save contact locally as pending
                    val db = AppDatabase.getDatabase(this@MainActivity)
                    val contact = Contact(
                        phoneHash = contactHash,
                        displayName = phoneNumber,
                        isConnected = false
                    )
                    db.contactDao().insertContact(contact)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to send request", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startMessagePolling() {
        val intent = Intent(this, MessagePollingService::class.java)
        startService(intent)
    }
    
    override fun onContactClick(contact: ConnectedUser) {
        if (!contact.isConnected) {
            Toast.makeText(this, "Please wait for contact to accept connection", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("CONTACT_HASH", contact.phoneHash)
        intent.putExtra("CONTACT_NAME", contact.displayName)
        startActivity(intent)
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
