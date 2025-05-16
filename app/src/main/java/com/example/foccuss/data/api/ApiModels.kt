package com.example.foccuss.data.api

import kotlinx.serialization.Serializable

@Serializable
data class BlockedAppDto(
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean
)

@Serializable
data class BlockTimeSettingsDto(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val monday: Boolean,
    val tuesday: Boolean,
    val wednesday: Boolean,
    val thursday: Boolean,
    val friday: Boolean,
    val saturday: Boolean,
    val sunday: Boolean,
    val isActive: Boolean
)

@Serializable
data class DataExportDto(
    val blockedApps: List<BlockedAppDto>,
    val blockTimeSettings: BlockTimeSettingsDto
)

@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String
) 