package com.securemessenger.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long
    
    @Update
    suspend fun updateContact(contact: Contact)
    
    @Delete
    suspend fun deleteContact(contact: Contact)
    
    @Query("SELECT * FROM contacts ORDER BY displayName ASC")
    fun getAllContacts(): Flow<List<Contact>>
    
    @Query("SELECT * FROM contacts WHERE phoneHash = :phoneHash")
    suspend fun getContactByPhoneHash(phoneHash: String): Contact?
    
    @Query("SELECT * FROM contacts WHERE isConnected = 1 ORDER BY lastMessageTime DESC")
    fun getConnectedContacts(): Flow<List<Contact>>
    
    @Query("UPDATE contacts SET isConnected = 1 WHERE phoneHash = :phoneHash")
    suspend fun markContactAsConnected(phoneHash: String)
    
    @Query("SELECT * FROM contacts WHERE phoneHash = :phoneHash")
    suspend fun isContactConnected(phoneHash: String): Boolean
}
