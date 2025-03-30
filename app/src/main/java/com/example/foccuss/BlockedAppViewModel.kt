package com.example.foccuss.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.foccuss.FoccussApplication
import com.example.foccuss.data.entity.BlockedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BlockedAppViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = FoccussApplication.database.blockedAppDao()
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
}