package com.securemessenger.utils

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurityUtils {
    
    private const val PREF_NAME = "secure_messenger_prefs"
    private const val PHONE_HASH_KEY = "phone_hash"
    private const val USER_TOKEN_KEY = "user_token"
    private const val IS_LOGGED_IN_KEY = "is_logged_in"
    
    private lateinit var encryptedPrefs: SharedPreferences
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    
    fun initialize(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Hash mobile number using SHA-256
     * Server only stores this hash, never the actual number
     */
    fun hashPhoneNumber(phoneNumber: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(phoneNumber.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Save user session after successful login
     */
    fun saveUserSession(phoneHash: String, token: String) {
        encryptedPrefs.edit().apply {
            putString(PHONE_HASH_KEY, phoneHash)
            putString(USER_TOKEN_KEY, token)
            putBoolean(IS_LOGGED_IN_KEY, true)
            apply()
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return encryptedPrefs.getBoolean(IS_LOGGED_IN_KEY, false)
    }
    
    /**
     * Get stored phone hash
     */
    fun getPhoneHash(): String? {
        return encryptedPrefs.getString(PHONE_HASH_KEY, null)
    }
    
    /**
     * Get user token
     */
    fun getUserToken(): String? {
        return encryptedPrefs.getString(USER_TOKEN_KEY, null)
    }
    
    /**
     * Clear user session (logout)
     */
    fun clearSession() {
        encryptedPrefs.edit().clear().apply()
    }
    
    /**
     * Encrypt message for local storage
     */
    fun encryptMessage(message: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        
        val encryptedBytes = cipher.doFinal(message.toByteArray())
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }
    
    /**
     * Decrypt message from local storage
     */
    fun decryptMessage(encryptedMessage: String, secretKey: SecretKey): String {
        val combined = Base64.decode(encryptedMessage, Base64.DEFAULT)
        val iv = combined.copyOf(GCM_IV_LENGTH)
        val encryptedBytes = combined.sliceArray(GCM_IV_LENGTH until combined.size)
        
        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
    
    /**
     * Generate a new AES key for encryption
     */
    fun generateAESKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }
}
