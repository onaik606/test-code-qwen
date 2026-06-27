package com.example.securemessenger.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MessageRepository(private val messageDao: MessageDao) {
    
    fun getMessagesBetweenUsers(userId1: String, userId2: String): Flow<List<Message>> {
        return messageDao.getMessagesBetweenUsers(userId1, userId2)
    }
    
    suspend fun sendMessage(senderId: String, receiverId: String, content: String): Message {
        val message = Message(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            timestamp = System.currentTimeMillis(),
            isDelivered = false,
            isRead = false
        )
        
        messageDao.insertMessage(message)
        return message
    }
    
    suspend fun markMessageAsDelivered(messageId: String) {
        val message = messageDao.getMessageById(messageId)
        message?.let {
            messageDao.updateMessage(it.copy(isDelivered = true))
        }
    }
    
    suspend fun markMessageAsRead(messageId: String) {
        val message = messageDao.getMessageById(messageId)
        message?.let {
            messageDao.updateMessage(it.copy(isRead = true))
        }
    }
    
    suspend fun getUndeliveredMessagesForUser(userId: String): List<Message> {
        return messageDao.getUndeliveredMessagesForUser(userId)
    }
}
