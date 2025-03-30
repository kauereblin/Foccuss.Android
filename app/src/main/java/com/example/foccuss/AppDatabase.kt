package com.example.foccuss.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.foccuss.data.dao.BlockedAppDao
import com.example.foccuss.data.entity.BlockedApp

@Database(entities = [BlockedApp::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
}