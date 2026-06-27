package com.securemessenger.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneHash: String,
    val displayName: String,
    val isConnected: Boolean = false,
    val lastMessageTime: Long? = null
)
