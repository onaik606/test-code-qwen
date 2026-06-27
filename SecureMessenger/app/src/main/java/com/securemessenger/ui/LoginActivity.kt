package com.securemessenger.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.securemessenger.databinding.ActivityLoginBinding
import com.securemessenger.model.AppDatabase
import com.securemessenger.network.ApiClient
import com.securemessenger.network.AuthResponse
import com.securemessenger.network.OtpRequest
import com.securemessenger.network.VerifyOtpRequest
import com.securemessenger.utils.SecurityUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    
    // TESTING MODE - Set to true to disable OTP verification
    private val TESTING_MODE = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    override fun onStart() {
        super.onStart()
        
        // Check if user is already logged in
        if (SecurityUtils.isLoggedIn()) {
            navigateToMainActivity()
        }
    }
    
    private fun setupUI() {
        binding.btnSendOtp.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            
            if (phoneNumber.isEmpty()) {
                binding.etPhoneNumber.error = "Please enter phone number"
                return@setOnClickListener
            }
            
            if (!isValidPhoneNumber(phoneNumber)) {
                binding.etPhoneNumber.error = "Invalid phone number"
                return@setOnClickListener
            }
            
            if (TESTING_MODE) {
                // In testing mode, auto-verify without OTP
                Toast.makeText(this, "Testing Mode: Auto-verifying...", Toast.LENGTH_SHORT).show()
                verifyUser(phoneNumber, "123456") // Mock OTP
            } else {
                // Production mode - send actual OTP
                sendOtp(phoneNumber)
            }
        }
        
        binding.btnVerifyOtp.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            val otp = binding.etOtp.text.toString().trim()
            
            if (otp.isEmpty()) {
                binding.etOtp.error = "Please enter OTP"
                return@setOnClickListener
            }
            
            verifyUser(phoneNumber, otp)
        }
    }
    
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic validation - at least 10 digits
        return phoneNumber.length >= 10 && phoneNumber.all { it.isDigit() }
    }
    
    private fun sendOtp(phoneNumber: String) {
        lifecycleScope.launch {
            try {
                val phoneHash = SecurityUtils.hashPhoneNumber(phoneNumber)
                val request = OtpRequest(phoneHash)
                
                val response = ApiClient.apiService.requestOtp(request)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "OTP sent successfully", Toast.LENGTH_SHORT).show()
                    binding.layoutOtp.visibility = android.view.View.VISIBLE
                } else {
                    Toast.makeText(this@LoginActivity, "Failed to send OTP", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun verifyUser(phoneNumber: String, otp: String) {
        lifecycleScope.launch {
            try {
                val phoneHash = SecurityUtils.hashPhoneNumber(phoneNumber)
                val request = VerifyOtpRequest(phoneHash, otp)
                
                val response = ApiClient.apiService.verifyOtp(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val authResponse = response.body()!!
                    
                    // Save session with phone hash (NOT the actual number)
                    SecurityUtils.saveUserSession(
                        phoneHash = authResponse.phoneHash,
                        token = authResponse.token
                    )
                    
                    // Create local database entry for user
                    val db = AppDatabase.getDatabase(this@LoginActivity)
                    
                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (TESTING_MODE) {
                    // In testing mode, simulate successful login even without server
                    val phoneHash = SecurityUtils.hashPhoneNumber(phoneNumber)
                    SecurityUtils.saveUserSession(
                        phoneHash = phoneHash,
                        token = "test_token_${System.currentTimeMillis()}"
                    )
                    Toast.makeText(this@LoginActivity, "Testing Mode: Login successful", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
