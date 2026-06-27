package com.securemessenger

import android.app.Application
import com.securemessenger.utils.SecurityUtils

class SecureMessengerApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize security utilities
        SecurityUtils.initialize(this)
    }
    
    companion object {
        lateinit var instance: SecureMessengerApp
            private set
    }
}
