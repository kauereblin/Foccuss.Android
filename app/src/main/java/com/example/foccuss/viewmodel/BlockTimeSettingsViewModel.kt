package com.example.foccuss.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.foccuss.FoccussApplication
import com.example.foccuss.data.entity.BlockTimeSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class BlockTimeSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = FoccussApplication.database.blockTimeSettingsDao()
    val settings: LiveData<BlockTimeSettings?> = dao.getSettings()
    
    private val TAG = "BlockTimeSettings"
    
    suspend fun saveSettings(settings: BlockTimeSettings) {
        Log.d(TAG, "Saving block time settings: $settings")
        withContext(Dispatchers.IO) {
            dao.insert(settings)
            Log.d(TAG, "Block time settings saved successfully")
        }
    }
    
    suspend fun isBlockingActiveNow(): Boolean {
        return withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // Sunday is 1, Monday is 2, etc.
            
            val isActive = dao.isBlockingActiveNow(currentHour, currentMinute, dayOfWeek)
            Log.d(TAG, "Checking if blocking is active now: $isActive (Day: $dayOfWeek, Time: $currentHour:$currentMinute)")
            return@withContext isActive
        }
    }
} 