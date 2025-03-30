package com.example.foccuss

import android.app.Application
import androidx.room.Room
import com.example.foccuss.data.AppDatabase

class FoccussApplication : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "foccuss-database"
        ).build()
    }
}