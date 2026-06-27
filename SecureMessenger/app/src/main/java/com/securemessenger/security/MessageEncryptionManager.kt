package com.securemessenger.security

import android.content.Context
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

/**
 * Manages message encryption and decryption for local storage
 * Messages are encrypted before storing locally and decrypted when reading
 */
class MessageEncryptionManager(private val context: Context) {
    
    private val keyStore = KeyStore.getInstance("AndroidKeyStore")
    private val aesMode = "AES/GCM/NoPadding"
    private val gcmIvLength = 12
    private val gcmTagLength = 128
    
    init {
        keyStore.load(null)
    }
    
    /**
     * Get or create encryption key for a specific conversation
     */
    fun getOrCreateKey(conversationId: String): SecretKey {
        val alias = "msg_key_$conversationId"
        
        return try {
            val entry = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey ?: createKey(alias)
        } catch (e: Exception) {
            createKey(alias)
        }
    }
    
    private fun createKey(alias: String): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        keyGen.init(
            256,
            android.security.keystore.KeyGenParameterSpec.Builder(
                alias,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or 
                android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGen.generateKey()
    }
    
    /**
     * Encrypt message content
     */
    fun encrypt(message: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(aesMode)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        
        val encryptedBytes = cipher.doFinal(message.toByteArray())
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }
    
    /**
     * Decrypt message content
     */
    fun decrypt(encryptedMessage: String, secretKey: SecretKey): String {
        val combined = Base64.decode(encryptedMessage, Base64.DEFAULT)
        val iv = combined.copyOf(gcmIvLength)
        val encryptedBytes = combined.sliceArray(gcmIvLength until combined.size)
        
        val cipher = Cipher.getInstance(aesMode)
        val spec = GCMParameterSpec(gcmTagLength, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
}
