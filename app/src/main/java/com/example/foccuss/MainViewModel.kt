package com.example.foccuss.viewmodel

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

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
}