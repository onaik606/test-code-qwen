package com.example.securemessenger.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connections WHERE (userId1 = :userId1 AND userId2 = :userId2) OR (userId1 = :userId2 AND userId2 = :userId1)")
    suspend fun getConnectionBetweenUsers(userId1: String, userId2: String): Connection?
    
    @Query("SELECT * FROM connections WHERE userId1 = :userId OR userId2 = :userId")
    fun getConnectionsForUser(userId: String): Flow<List<Connection>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: Connection)
    
    @Update
    suspend fun updateConnection(connection: Connection)
}
