package com.example.securemessenger.utils

object Config {
    const val DISABLE_OTP_FOR_TESTING = true
    
    const val BASE_URL = "https://your-server.com/api/"
    
    const val POLLING_INTERVAL_MS = 5000L
    
    const val SHARED_PREFS_NAME = "secure_messenger_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_PHONE_HASH = "phone_hash"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
}
