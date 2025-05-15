package com.example.foccuss.data.api

import android.content.Context
import android.util.Log
import com.example.foccuss.FoccussApplication
import com.example.foccuss.data.entity.BlockTimeSettings
import com.example.foccuss.data.entity.BlockedApp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ApiService(private val context: Context) {
    private val TAG = "ApiService"
    private val apiConfig = FoccussApplication.apiConfig
    
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    suspend fun exportBlockedApps(blockedApps: List<BlockedApp>): Result<ApiResponse> {
        return try {
            Log.d(TAG, "Exporting ${blockedApps.size} blocked apps to web API")
            
            val blockedAppDtos = blockedApps.map { app ->
                BlockedAppDto(
                    packageName = app.packageName,
                    appName = app.appName,
                    isBlocked = app.isBlocked
                )
            }
            
            val apiUrl = apiConfig.getApiUrl()
            val response = client.post("$apiUrl/blocked-apps") {
                contentType(ContentType.Application.Json)
                setBody(blockedAppDtos)
            }
            
            val apiResponse = response.body<ApiResponse>()
            Log.d(TAG, "Blocked apps export response: $apiResponse")
            Result.success(apiResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting blocked apps: ${e.message}", e)
            Log.e(TAG, "Error class: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }
    
    suspend fun exportBlockTimeSettings(settings: BlockTimeSettings): Result<ApiResponse> {
        return try {
            Log.d(TAG, "Exporting block time settings to web API")
            
            val settingsDto = BlockTimeSettingsDto(
                startHour = settings.startHour,
                startMinute = settings.startMinute,
                endHour = settings.endHour,
                endMinute = settings.endMinute,
                monday = settings.monday,
                tuesday = settings.tuesday,
                wednesday = settings.wednesday,
                thursday = settings.thursday,
                friday = settings.friday,
                saturday = settings.saturday,
                sunday = settings.sunday,
                isActive = settings.isActive
            )

            val apiUrl = apiConfig.getApiUrl()
            val response = client.post("$apiUrl/block-time-settings") {
                contentType(ContentType.Application.Json)
                setBody(settingsDto)
            }
            
            val apiResponse = response.body<ApiResponse>()
            Log.d(TAG, "Block time settings export response: $apiResponse")
            Result.success(apiResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting block time settings: ${e.message}", e)
            Log.e(TAG, "Error class: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }
    
    suspend fun exportAllData(blockedApps: List<BlockedApp>, settings: BlockTimeSettings): Result<ApiResponse> {
        return try {
            Log.d(TAG, "Exporting all data to web API")
            
            val blockedAppDtos = blockedApps.map { app ->
                BlockedAppDto(
                    packageName = app.packageName,
                    appName = app.appName,
                    isBlocked = app.isBlocked
                )
            }
            
            val settingsDto = BlockTimeSettingsDto(
                startHour = settings.startHour,
                startMinute = settings.startMinute,
                endHour = settings.endHour,
                endMinute = settings.endMinute,
                monday = settings.monday,
                tuesday = settings.tuesday,
                wednesday = settings.wednesday,
                thursday = settings.thursday,
                friday = settings.friday,
                saturday = settings.saturday,
                sunday = settings.sunday,
                isActive = settings.isActive
            )
            
            val dataExport = DataExportDto(
                blockedApps = blockedAppDtos,
                blockTimeSettings = settingsDto
            )

            val apiUrl = apiConfig.getApiUrl()
            val response = client.post("$apiUrl/export-all") {
                contentType(ContentType.Application.Json)
                setBody(dataExport)
            }
            
            val apiResponse = response.body<ApiResponse>()
            Log.d(TAG, "All data export response: $apiResponse")
            Result.success(apiResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting all data: ${e.message}", e)
            Log.e(TAG, "Error class: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }
} 