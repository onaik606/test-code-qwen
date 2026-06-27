package com.securemessenger.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderPhoneHash: String,
    val receiverPhoneHash: String,
    val content: String, // Encrypted content
    val timestamp: Long,
    val isDelivered: Boolean = false,
    val isRead: Boolean = false,
    val messageType: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO
}
