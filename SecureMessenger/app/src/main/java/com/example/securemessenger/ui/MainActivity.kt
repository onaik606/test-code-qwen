package com.example.securemessenger.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.securemessenger.SecureMessengerApp
import com.example.securemessenger.data.UserRepository
import com.example.securemessenger.databinding.ActivityMainBinding
import com.example.securemessenger.utils.Config
import com.example.securemessenger.utils.HashUtils
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepository: UserRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val app = application as SecureMessengerApp
        userRepository = UserRepository(app.database.userDao())
        
        checkLoginStatus()
        
        binding.btnRegister.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                registerUser(phoneNumber)
            } else {
                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun checkLoginStatus() {
        val prefs = getSharedPreferences(Config.SHARED_PREFS_NAME, MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean(Config.PREF_IS_LOGGED_IN, false)
        val userId = prefs.getString(Config.PREF_USER_ID, null)
        
        if (isLoggedIn && userId != null) {
            navigateToChatList(userId)
        }
    }
    
    private fun registerUser(phoneNumber: String) {
        lifecycleScope.launch {
            try {
                // In testing mode with OTP disabled, register directly
                if (Config.DISABLE_OTP_FOR_TESTING) {
                    val user = userRepository.registerUser(phoneNumber, null)
                    saveLoginState(user.id, HashUtils.hashPhoneNumber(phoneNumber))
                    navigateToChatList(user.id)
                } else {
                    // TODO: Implement OTP verification flow
                    Toast.makeText(this@MainActivity, "OTP verification not implemented yet", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun saveLoginState(userId: String, phoneHash: String) {
        val prefs = getSharedPreferences(Config.SHARED_PREFS_NAME, MODE_PRIVATE)
        prefs.edit().apply {
            putString(Config.PREF_USER_ID, userId)
            putString(Config.PREF_PHONE_HASH, phoneHash)
            putBoolean(Config.PREF_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    private fun navigateToChatList(userId: String) {
        val intent = Intent(this, ChatListActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
        finish()
    }
}
