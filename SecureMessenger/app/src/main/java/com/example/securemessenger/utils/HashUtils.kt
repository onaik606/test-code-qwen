package com.example.securemessenger.utils

import java.security.MessageDigest

object HashUtils {
    
    fun hashPhoneNumber(phoneNumber: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(phoneNumber.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    fun verifyPhoneNumber(phoneNumber: String, hashedPhoneNumber: String): Boolean {
        return hashPhoneNumber(phoneNumber) == hashedPhoneNumber
    }
}
