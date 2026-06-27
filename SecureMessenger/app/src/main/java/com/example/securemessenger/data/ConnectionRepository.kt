package com.example.securemessenger.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ConnectionRepository(private val connectionDao: ConnectionDao) {
    
    suspend fun createConnection(userId1: String, userId2: String): Connection {
        val connection = Connection(
            id = UUID.randomUUID().toString(),
            userId1 = userId1,
            userId2 = userId2,
            status = ConnectionStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        
        connectionDao.insertConnection(connection)
        return connection
    }
    
    suspend fun getConnectionBetweenUsers(userId1: String, userId2: String): Connection? {
        return connectionDao.getConnectionBetweenUsers(userId1, userId2)
    }
    
    fun getConnectionsForUser(userId: String): Flow<List<Connection>> {
        return connectionDao.getConnectionsForUser(userId)
    }
    
    suspend fun acceptConnection(connectionId: String, connection: Connection) {
        connectionDao.updateConnection(connection.copy(status = ConnectionStatus.ACCEPTED))
    }
    
    suspend fun rejectConnection(connectionId: String, connection: Connection) {
        connectionDao.updateConnection(connection.copy(status = ConnectionStatus.REJECTED))
    }
}
