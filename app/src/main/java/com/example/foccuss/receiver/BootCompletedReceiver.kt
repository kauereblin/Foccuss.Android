package com.example.foccuss.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.foccuss.service.FoccussForegroundService

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Verificar se o serviço de acessibilidade está habilitado
            val accessibilityEnabled = isAccessibilityServiceEnabled(context)

            if (accessibilityEnabled) {
                // Iniciar o serviço em primeiro plano
                val serviceIntent = Intent(context, FoccussForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
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