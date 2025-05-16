package com.example.foccuss.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.foccuss.FoccussApplication
import com.example.foccuss.data.api.ApiResponse
import com.example.foccuss.data.entity.BlockedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BlockedAppViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = FoccussApplication.database.blockedAppDao()
    private val apiService = FoccussApplication.apiService
    val blockedApps: LiveData<List<BlockedApp>> = dao.getAllBlockedApps()

    private val TAG = "BlockedAppViewModel"
    private var pendingChanges = mutableMapOf<String, Boolean>()

    suspend fun addBlockedApp(app: BlockedApp) {
        Log.d(TAG, "Adding app to pending changes: ${app.packageName}")
        pendingChanges[app.packageName] = true
    }

    suspend fun removeBlockedApp(app: BlockedApp) {
        Log.d(TAG, "Removing app from pending changes: ${app.packageName}")
        pendingChanges[app.packageName] = false
    }

    suspend fun saveChanges() {
        Log.d(TAG, "Saving ${pendingChanges.size} changes to database")
        withContext(Dispatchers.IO) {
            pendingChanges.forEach { (packageName, isBlocked) ->
                val app = BlockedApp(
                    packageName = packageName,
                    appName = "", // This will be updated by the UI
                    isBlocked = isBlocked
                )
                if (isBlocked) {
                    dao.insert(app)
                    Log.d(TAG, "Inserted blocked app: $packageName")
                } else {
                    dao.delete(app)
                    Log.d(TAG, "Deleted blocked app: $packageName")
                }
            }
            pendingChanges.clear()
            Log.d(TAG, "All changes saved successfully")
        }
    }
    
    suspend fun exportBlockedApps(): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            val apps = blockedApps.value ?: listOf()
            Log.d(TAG, "Exporting ${apps.size} blocked apps")
            apiService.exportBlockedApps(apps)
        }
    }
    
    suspend fun syncFromWeb(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val result = apiService.fetchBlockedApps()
                result.onSuccess { webApps ->
                    Log.d(TAG, "Successfully fetched ${webApps.size} blocked apps from web")
                    
                    var insertCount = 0
                    webApps.forEach { webApp ->
                        val app = BlockedApp(
                            packageName = webApp.packageName,
                            appName = webApp.appName,
                            isBlocked = webApp.isBlocked
                        )
                        dao.insert(app)
                        insertCount++
                    }
                    
                    Log.d(TAG, "Synced $insertCount blocked apps from web")
                    return@withContext Result.success(insertCount)
                }
                
                result.onFailure { error ->
                    Log.e(TAG, "Failed to fetch blocked apps from web: ${error.message}")
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