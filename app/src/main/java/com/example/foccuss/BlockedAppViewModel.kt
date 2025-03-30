package com.example.foccuss.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.foccuss.FoccussApplication
import com.example.foccuss.data.entity.BlockedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BlockedAppViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = FoccussApplication.database.blockedAppDao()
    val blockedApps: LiveData<List<BlockedApp>> = dao.getAllBlockedApps()

    suspend fun addBlockedApp(app: BlockedApp) {
        withContext(Dispatchers.IO) {
            dao.insert(app.copy(isBlocked = true))
        }
    }

    suspend fun removeBlockedApp(app: BlockedApp) {
        withContext(Dispatchers.IO) {
            dao.delete(app)
        }
    }
}