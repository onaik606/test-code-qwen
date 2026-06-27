package com.example.securemessenger

import android.app.Application
import com.example.securemessenger.data.AppDatabase

class SecureMessengerApp : Application() {
    
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: SecureMessengerApp
            private set
    }
}
