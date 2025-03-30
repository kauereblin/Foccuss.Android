package com.example.foccuss.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_time_settings")
data class BlockTimeSettings(
    @PrimaryKey val id: Int = 1, // Using a fixed ID for singleton pattern
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 0,
    val monday: Boolean = true,
    val tuesday: Boolean = true,
    val wednesday: Boolean = true,
    val thursday: Boolean = true,
    val friday: Boolean = true,
    val saturday: Boolean = false,
    val sunday: Boolean = false,
    val isActive: Boolean = true
) 