package com.example.foccuss.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.foccuss.FoccussApplication
import com.example.foccuss.ui.OverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoccussAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private val TAG = "FoccussAccessibility"

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            Log.d(TAG, "Window state changed for package: $packageName")

            // Ignore our own package
            if (packageName == "com.example.foccuss") {
                Log.d(TAG, "Ignoring own package")
                return
            }

            serviceScope.launch {
                val isBlocked = withContext(Dispatchers.IO) {
                    FoccussApplication.database.blockedAppDao().isAppBlocked(packageName)
                }

                if (isBlocked) {
                    Log.d(TAG, "App is blocked: $packageName")
                    val overlayIntent = Intent(this@FoccussAccessibilityService, OverlayActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("PACKAGE_NAME", packageName)
                    }
                    startActivity(overlayIntent)
                    Log.d(TAG, "Started overlay activity for: $packageName")
                } else {
                    Log.d(TAG, "App is not blocked: $packageName")
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Accessibility service connected")
        // Iniciar o serviço em primeiro plano
        val intent = Intent(this, FoccussForegroundService::class.java)
        startService(intent)
        Log.d(TAG, "Started foreground service")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "Accessibility service destroyed")
    }
}