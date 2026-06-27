package com.example.securemessenger.network

import com.example.securemessenger.data.Connection
import com.example.securemessenger.data.Message
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>
    
    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("connections")
    suspend fun createConnection(@Body request: ConnectionRequest): Response<ConnectionResponse>
    
    @GET("connections/{userId}")
    suspend fun getConnections(@Path("userId") userId: String): Response<List<Connection>>
    
    @PUT("connections/{connectionId}/accept")
    suspend fun acceptConnection(@Path("connectionId") connectionId: String): Response<Unit>
    
    @PUT("connections/{connectionId}/reject")
    suspend fun rejectConnection(@Path("connectionId") connectionId: String): Response<Unit>
    
    @POST("messages")
    suspend fun sendMessage(@Body request: MessageRequest): Response<MessageResponse>
    
    @GET("messages/pending/{userId}")
    suspend fun getPendingMessages(@Path("userId") userId: String): Response<List<Message>>
}

data class RegisterRequest(val phoneHash: String, val displayName: String?)
data class RegisterResponse(val userId: String, val success: Boolean)

data class LoginRequest(val phoneHash: String)
data class LoginResponse(val userId: String, val success: Boolean)

data class ConnectionRequest(val userId1: String, val userId2: String)
data class ConnectionResponse(val connectionId: String, val success: Boolean)

data class MessageRequest(val senderId: String, val receiverId: String, val content: String)
data class MessageResponse(val messageId: String, val success: Boolean)
