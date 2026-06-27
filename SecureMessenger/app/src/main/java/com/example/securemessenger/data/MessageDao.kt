package com.example.securemessenger.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    fun getMessagesBetweenUsers(userId1: String, userId2: String): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): Message?
    
    @Query("SELECT * FROM messages WHERE senderId = :userId AND isDelivered = 0")
    suspend fun getUndeliveredMessagesForUser(userId: String): List<Message>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
    
    @Update
    suspend fun updateMessage(message: Message)
    
    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}
