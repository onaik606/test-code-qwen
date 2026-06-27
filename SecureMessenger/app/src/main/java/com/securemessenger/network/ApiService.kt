package com.securemessenger.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    
    /**
     * Request OTP for phone number verification
     * In testing phase, this is disabled and auto-verifies
     */
    @POST("auth/request-otp")
    suspend fun requestOtp(@Body request: OtpRequest): Response<ApiResponse<Unit>>
    
    /**
     * Verify OTP and get authentication token
     * Returns only the phone hash, never the actual number
     */
    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>
    
    /**
     * Search for a user by phone hash
     */
    @GET("users/search")
    suspend fun searchUser(@Query("phoneHash") phoneHash: String): Response<UserSearchResponse>
    
    /**
     * Send connection request to another user
     * Both users must accept before they can chat
     */
    @POST("connections/request")
    suspend fun sendConnectionRequest(@Body request: ConnectionRequest): Response<ApiResponse<Unit>>
    
    /**
     * Accept connection request
     */
    @POST("connections/accept")
    suspend fun acceptConnectionRequest(@Body request: ConnectionRequest): Response<ApiResponse<Unit>>
    
    /**
     * Get list of connected users
     */
    @GET("connections/list")
    suspend fun getConnectedUsers(@Query("userHash") userHash: String): Response<ConnectedUsersResponse>
    
    /**
     * Check if receiver is online and fetch pending messages
     * This is called by the polling mechanism
     */
    @GET("messages/pending")
    suspend fun getPendingMessages(
        @Query("senderHash") senderHash: String,
        @Query("receiverHash") receiverHash: String
    ): Response<PendingMessagesResponse>
    
    /**
     * Mark message as delivered on server (for acknowledgment)
     * Note: Actual message content is NOT stored on server
     */
    @POST("messages/delivered")
    suspend fun markMessageDelivered(@Body request: DeliveryAckRequest): Response<ApiResponse<Unit>>
}

// Request/Response Models

data class OtpRequest(
    val phoneHash: String
)

data class VerifyOtpRequest(
    val phoneHash: String,
    val otp: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val phoneHash: String,
    val message: String? = null
)

data class UserSearchResponse(
    val found: Boolean,
    val phoneHash: String?,
    val displayName: String? = null
)

data class ConnectionRequest(
    val fromUserHash: String,
    val toUserHash: String
)

data class ConnectedUsersResponse(
    val users: List<ConnectedUser>
)

data class ConnectedUser(
    val phoneHash: String,
    val displayName: String?,
    val isConnected: Boolean,
    val lastSeen: Long?
)

data class PendingMessagesResponse(
    val messages: List<PendingMessage>,
    val hasMore: Boolean
)

data class PendingMessage(
    val messageId: String,
    val senderHash: String,
    val receiverHash: String,
    val timestamp: Long
    // Note: No message content - content stays on sender's device
)

data class DeliveryAckRequest(
    val messageId: String,
    val senderHash: String,
    val receiverHash: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null
)
