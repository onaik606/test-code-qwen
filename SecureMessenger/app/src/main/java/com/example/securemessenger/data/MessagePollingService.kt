package com.example.securemessenger.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.securemessenger.SecureMessengerApp
import com.example.securemessenger.ui.ChatListActivity
import com.example.securemessenger.utils.Config
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class MessagePollingService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "message_polling_channel"
        const val NOTIFICATION_ID = 1001
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startPolling()
        return START_STICKY
    }
    
    private fun startPolling() {
        pollingJob = serviceScope.launch {
            try {
                // Get current user ID from shared preferences
                val prefs = getSharedPreferences(Config.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                val userId = prefs.getString(Config.PREF_USER_ID, null) ?: return@launch
                
                val app = application as SecureMessengerApp
                val messageRepository = com.example.securemessenger.data.MessageRepository(app.database.messageDao())
                
                while (isActive) {
                    // Check for undelivered messages
                    val undeliveredMessages = messageRepository.getUndeliveredMessagesForUser(userId)
                    
                    // TODO: Here you would call the server to check if receiver is online
                    // and deliver messages. For now, we just mark them as delivered
                    // when the polling service runs (simulating receiver coming online)
                    
                    for (message in undeliveredMessages) {
                        messageRepository.markMessageAsDelivered(message.id)
                    }
                    
                    delay(Config.POLLING_INTERVAL_MS)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Message Polling",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps checking for message delivery status"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): android.app.Notification {
        val intent = Intent(this, ChatListActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Secure Messenger")
            .setContentText("Checking for message delivery...")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        pollingJob?.cancel()
        serviceScope.cancel()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
