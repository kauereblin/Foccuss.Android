package com.example.foccuss.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.foccuss.data.entity.BlockTimeSettings

@Dao
interface BlockTimeSettingsDao {
    @Query("SELECT * FROM block_time_settings WHERE id = 1")
    fun getSettings(): LiveData<BlockTimeSettings?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: BlockTimeSettings)
    
    @Update
    suspend fun update(settings: BlockTimeSettings)
    
    @Query("SELECT EXISTS(SELECT 1 FROM block_time_settings WHERE id = 1 AND isActive = 1)")
    fun isBlockingActive(): Boolean
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM block_time_settings 
            WHERE id = 1 AND isActive = 1 
            AND (
                (CASE :dayOfWeek
                    WHEN 2 THEN monday
                    WHEN 3 THEN tuesday
                    WHEN 4 THEN wednesday
                    WHEN 5 THEN thursday
                    WHEN 6 THEN friday
                    WHEN 7 THEN saturday
                    WHEN 1 THEN sunday
                    ELSE 0
                END) = 1
            )
            AND (
                (:currentHour > startHour OR (:currentHour = startHour AND :currentMinute >= startMinute))
                AND
                (:currentHour < endHour OR (:currentHour = endHour AND :currentMinute <= endMinute))
            )
        )
    """)
    fun isBlockingActiveNow(currentHour: Int, currentMinute: Int, dayOfWeek: Int): Boolean
} 