package com.example.foccuss.viewmodel

import android.app.Application
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.foccuss.FoccussApplication
import com.example.foccuss.data.entity.BlockTimeSettings
import com.example.foccuss.data.entity.BlockedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MainViewModel"
    private val apiService = FoccussApplication.apiService
    private val blockedAppDao = FoccussApplication.database.blockedAppDao()
    private val blockTimeSettingsDao = FoccussApplication.database.blockTimeSettingsDao()

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val accessibilityEnabled = try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            0
        }

        if (accessibilityEnabled == 1) {
            val services = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return services.contains("${context.packageName}/${context.packageName}.service.FoccussAccessibilityService")
        }

        return false
    }
    
    suspend fun syncAllDataFromWeb(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting sync of all data from web")
                
                val result = apiService.fetchAllData()
                result.onSuccess { webData ->
                    // Sync blocked apps
                    val blockedApps = webData.blockedApps
                    Log.d(TAG, "Syncing ${blockedApps.size} blocked apps from web")
                    
                    // Insert web apps
                    var insertCount = 0
                    blockedApps.forEach { webApp ->
                        val app = BlockedApp(
                            packageName = webApp.packageName,
                            appName = webApp.appName,
                            isBlocked = webApp.isBlocked
                        )
                        blockedAppDao.insertSync(app)
                        insertCount++
                    }
                    Log.d(TAG, "Synced $insertCount blocked apps from web")
                    
                    // Sync block time settings
                    val webSettings = webData.blockTimeSettings
                    Log.d(TAG, "Syncing block time settings from web")
                    
                    val settings = BlockTimeSettings(
                        id = 1, // Using fixed ID as it's a singleton
                        startHour = webSettings.startHour,
                        startMinute = webSettings.startMinute,
                        endHour = webSettings.endHour,
                        endMinute = webSettings.endMinute,
                        monday = webSettings.monday,
                        tuesday = webSettings.tuesday,
                        wednesday = webSettings.wednesday,
                        thursday = webSettings.thursday,
                        friday = webSettings.friday,
                        saturday = webSettings.saturday,
                        sunday = webSettings.sunday,
                        isActive = webSettings.isActive
                    )
                    
                    blockTimeSettingsDao.insertSync(settings)
                    Log.d(TAG, "Synced block time settings from web")
                    
                    return@withContext Result.success(true)
                }
                
                result.onFailure { error ->
                    Log.e(TAG, "Failed to fetch data from web: ${error.message}")
                    return@withContext Result.failure(error)
                }
                
                // This is a fallback - onSuccess or onFailure should have returned already
                return@withContext Result.failure(Exception("Unknown error during sync"))
            } catch (e: Exception) {
                Log.e(TAG, "Exception during sync: ${e.message}")
                return@withContext Result.failure(e)
            }
        }
    }
}