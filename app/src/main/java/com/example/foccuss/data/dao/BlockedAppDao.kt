package com.example.foccuss.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.foccuss.data.entity.BlockedApp

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps")
    fun getAllBlockedApps(): LiveData<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    fun getActiveBlockedApps(): List<BlockedApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedApp: BlockedApp)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(blockedApp: BlockedApp)

    @Update
    suspend fun update(blockedApp: BlockedApp)

    @Delete
    suspend fun delete(blockedApp: BlockedApp)
    
    @Delete
    fun deleteSync(blockedApp: BlockedApp)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_apps WHERE packageName = :packageName AND isBlocked = 1)")
    fun isAppBlocked(packageName: String): Boolean
}