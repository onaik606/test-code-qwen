package com.example.securemessenger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val phoneHash: String,
    val displayName: String?,
    val createdAt: Long
)
