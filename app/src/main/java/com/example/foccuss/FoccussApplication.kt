package com.example.foccuss

import android.app.Application
import androidx.room.Room
import com.example.foccuss.data.AppDatabase
import com.example.foccuss.data.api.ApiConfig
import com.example.foccuss.data.api.ApiService

class FoccussApplication : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set
            
        lateinit var apiService: ApiService
            private set
            
        lateinit var apiConfig: ApiConfig
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "foccuss-database"
        ).build()
        
        apiConfig = ApiConfig(applicationContext)
        apiService = ApiService(applicationContext)
    }
}