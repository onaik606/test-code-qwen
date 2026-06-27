package com.example.securemessenger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connections")
data class Connection(
    @PrimaryKey val id: String,
    val userId1: String,
    val userId2: String,
    val status: ConnectionStatus,
    val createdAt: Long
)

enum class ConnectionStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
