package com.example.foccuss.ui

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.foccuss.databinding.ActivityMainBinding
import com.example.foccuss.service.FoccussForegroundService
import com.example.foccuss.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 100
        private const val ACCESSIBILITY_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupButtons()
        checkPermissions()
    }

    private fun setupButtons() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnBlockedApps.setOnClickListener {
            startActivity(Intent(this, BlockedAppSelectionActivity::class.java))
        }

        binding.btnServiceStatus.setOnClickListener {
            if (viewModel.isAccessibilityServiceEnabled(this)) {
                // Desabilitar serviço
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else {
                // Habilitar serviço
                requestAccessibilityPermission()
            }
        }

        binding.btnBatteryOptimization.setOnClickListener {
            val intent = Intent().apply {
                action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            }
            startActivity(intent)
        }

        binding.btnAutostart.setOnClickListener {
            // Abrir configurações MIUI para autostart
            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback para configurações normais
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }

    private fun checkPermissions() {
        // Verificar permissão de overlay
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        }

        // Atualizar status do serviço
        updateServiceStatus()
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
    }

    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivityForResult(intent, ACCESSIBILITY_PERMISSION_REQUEST_CODE)
    }

    private fun updateServiceStatus() {
        val isServiceEnabled = viewModel.isAccessibilityServiceEnabled(this)

        binding.tvServiceStatus.text = if (isServiceEnabled) {
            "Serviço ativo"
        } else {
            "Serviço inativo"
        }

        binding.btnServiceStatus.text = if (isServiceEnabled) {
            "Desabilitar serviço"
        } else {
            "Habilitar serviço"
        }

        // Iniciar serviço se tudo estiver OK
        if (isServiceEnabled && Settings.canDrawOverlays(this)) {
            startFoccussService()
        }
    }

    private fun startFoccussService() {
        val serviceIntent = Intent(this, FoccussForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE ||
            requestCode == ACCESSIBILITY_PERMISSION_REQUEST_CODE) {
            updateServiceStatus()
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }
}