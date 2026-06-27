package com.securemessenger.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long
    
    @Update
    suspend fun updateMessage(message: Message)
    
    @Delete
    suspend fun deleteMessage(message: Message)
    
    @Query("SELECT * FROM messages WHERE (senderPhoneHash = :userHash AND receiverPhoneHash = :contactHash) OR (senderPhoneHash = :contactHash AND receiverPhoneHash = :userHash) ORDER BY timestamp ASC")
    fun getMessagesForConversation(userHash: String, contactHash: String): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE senderPhoneHash = :userHash AND isDelivered = 0 ORDER BY timestamp ASC")
    suspend fun getUndeliveredMessages(userHash: String): List<Message>
    
    @Query("UPDATE messages SET isDelivered = 1 WHERE id = :messageId")
    suspend fun markMessageAsDelivered(messageId: Long)
    
    @Query("UPDATE messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markMessageAsRead(messageId: Long)
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)
    
    @Query("SELECT * FROM messages WHERE isDelivered = 0 ORDER BY timestamp ASC")
    suspend fun getAllPendingMessages(): List<Message>
}
