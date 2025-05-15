package com.example.foccuss.ui

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foccuss.FoccussApplication
import com.example.foccuss.data.api.ApiConfig
import com.example.foccuss.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val apiConfig = FoccussApplication.apiConfig
    private val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etApiUrl.setText(apiConfig.getApiUrl())

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnSaveApiUrl.setOnClickListener {
            val apiUrl = binding.etApiUrl.text.toString().trim()
            if (apiUrl.isNotEmpty()) {
                apiConfig.setApiUrl(apiUrl)
                val savedUrl = apiConfig.getApiUrl()
                Log.d(TAG, "API URL saved: $savedUrl")
                Toast.makeText(this, "URL da API salva com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, insira uma URL válida", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnAutostart.setOnClickListener {
            // Tentar abrir configurações MIUI para autostart
            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback para configurações normais se não for Xiaomi ou não encontrar a activity
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }

        binding.btnBatteryOptimization.setOnClickListener {
            val intent = Intent().apply {
                action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            }
            startActivity(intent)
        }

        binding.btnBackgroundRestrictions.setOnClickListener {
            // Tentar abrir configurações MIUI para restrições de segundo plano
            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.miui.powerkeeper",
                        "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                    )
                    putExtra("package_name", packageName)
                    putExtra("package_label", getString(com.example.foccuss.R.string.app_name))
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
}