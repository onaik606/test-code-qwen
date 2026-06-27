package com.securemessenger.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.securemessenger.R
import com.securemessenger.model.AppDatabase
import com.securemessenger.model.Message
import com.securemessenger.network.ApiClient
import com.securemessenger.utils.SecurityUtils
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

/**
 * Background service that polls for new messages from server
 * Similar to WhatsApp's polling mechanism
 * Messages are only delivered when receiver comes online
 */
class MessagePollingService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())
    
    companion object {
        private const val POLL_INTERVAL_MS = 5000L // 5 seconds
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "message_polling_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startPolling()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        handler.removeCallbacksAndMessages(null)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Message Polling",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service for message delivery"
                enableLights(false)
                enableVibration(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Secure Messenger")
            .setContentText("Listening for new messages...")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun startPolling() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                pollForMessages()
                handler.postDelayed(this, POLL_INTERVAL_MS)
            }
        }, POLL_INTERVAL_MS)
    }
    
    private fun pollForMessages() {
        serviceScope.launch {
            try {
                val userHash = SecurityUtils.getPhoneHash() ?: return@launch
                
                // Get all pending messages from local database
                val db = AppDatabase.getDatabase(applicationContext)
                val pendingMessages = db.messageDao().getAllPendingMessages()
                
                // For each pending message, check if receiver is online
                for (message in pendingMessages) {
                    checkAndDeliverMessage(message, userHash)
                }
                
                // Also check for incoming messages from connected users
                checkIncomingMessages(userHash)
                
            } catch (e: Exception) {
                // Log error but don't stop polling
                android.util.Log.e("MessagePolling", "Error polling: ${e.message}")
            }
        }
    }
    
    private suspend fun checkAndDeliverMessage(message: Message, userHash: String) {
        try {
            // Check if receiver is online by calling pending messages endpoint
            val response = ApiClient.apiService.getPendingMessages(
                senderHash = userHash,
                receiverHash = message.receiverPhoneHash
            )
            
            if (response.isSuccessful && response.body() != null) {
                // Receiver is online, mark message as delivered locally
                db.messageDao().markMessageAsDelivered(message.id)
                
                // Send delivery acknowledgment to server (without message content)
                val ackRequest = com.securemessenger.network.DeliveryAckRequest(
                    messageId = message.id.toString(),
                    senderHash = userHash,
                    receiverHash = message.receiverPhoneHash
                )
                
                try {
                    ApiClient.apiService.markMessageDelivered(ackRequest)
                } catch (e: Exception) {
                    // Acknowledgment failed, but message is marked as delivered locally
                }
            }
        } catch (e: Exception) {
            // Receiver offline, keep message in sender's phone
        }
    }
    
    private suspend fun checkIncomingMessages(userHash: String) {
        // This would check for messages sent to this user from connected contacts
        // In a real implementation, you'd query your connected contacts list
        // and check for pending messages from each
        
        // For now, this is a placeholder for the polling logic
        // The actual implementation depends on your server API structure
    }
}
