package com.example.securemessenger.data

import com.example.securemessenger.utils.HashUtils

class UserRepository(private val userDao: UserDao) {
    
    suspend fun registerUser(phoneNumber: String, displayName: String?): User {
        val phoneHash = HashUtils.hashPhoneNumber(phoneNumber)
        
        // Check if user already exists
        val existingUser = userDao.getUserByPhoneHash(phoneHash)
        if (existingUser != null) {
            return existingUser
        }
        
        // Create new user with unique ID
        val userId = java.util.UUID.randomUUID().toString()
        val user = User(
            id = userId,
            phoneHash = phoneHash,
            displayName = displayName,
            createdAt = System.currentTimeMillis()
        )
        
        userDao.insertUser(user)
        return user
    }
    
    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }
    
    suspend fun getUserByPhoneHash(phoneHash: String): User? {
        return userDao.getUserByPhoneHash(phoneHash)
    }
}
