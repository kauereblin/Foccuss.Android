package com.example.foccuss.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.foccuss.FoccussApplication
import com.example.foccuss.ui.OverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoccussAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Ignore our own package
            if (packageName == packageName) return

            serviceScope.launch {
                val isBlocked = withContext(Dispatchers.IO) {
                    FoccussApplication.database.blockedAppDao().isAppBlocked(packageName)
                }

                if (isBlocked) {
                    val overlayIntent = Intent(this@FoccussAccessibilityService, OverlayActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("PACKAGE_NAME", packageName)
                    }
                    startActivity(overlayIntent)
                }
            }
        }
    }

    override fun onInterrupt() {
        // Não é necessário implementar para este caso
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Iniciar o serviço em primeiro plano
        val intent = Intent(this, FoccussForegroundService::class.java)
        startService(intent)
    }
}