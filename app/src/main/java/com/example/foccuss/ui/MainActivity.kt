package com.example.foccuss.ui

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.foccuss.databinding.ActivityMainBinding
import com.example.foccuss.service.FoccussForegroundService
import com.example.foccuss.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val TAG = "MainActivity"

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
        
        syncFromWebAtStartup()
    }
    
    private fun syncFromWebAtStartup() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                binding.tvSyncStatus.text = "Sincronizando dados..."
                val result = viewModel.syncAllDataFromWeb()
                
                result.onSuccess {
                    Log.d(TAG, "Initial sync from web successful")
                    binding.tvSyncStatus.text = "Última sincronização: Agora mesmo"
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity, 
                            "Dados sincronizados com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.onFailure { error ->
                    Log.e(TAG, "Initial sync failed: ${error.message}")
                    if (error is UnknownHostException) {
                        // This is usually the case when the app is first started and there's no connection
                        binding.tvSyncStatus.text = "Usando dados locais (sem conexão)"
                    } else {
                        binding.tvSyncStatus.text = "Erro: ${error.message}"
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity, 
                                "Erro na sincronização: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during initial sync: ${e.message}")
                binding.tvSyncStatus.text = "Erro na sincronização"
            }
        }
    }

    private fun setupButtons() {
        binding.btnSettings.setOnClickListener {
            Log.d(TAG, "Opening Settings")
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnBlockedApps.setOnClickListener {
            Log.d(TAG, "Opening Blocked Apps Configuration")
            startActivity(Intent(this, BlockedAppSelectionActivity::class.java))
        }

        binding.btnBlockTimes.setOnClickListener {
            Log.d(TAG, "Opening Block Times Configuration")
            startActivity(Intent(this, BlockTimeSettingsActivity::class.java))
        }

        binding.btnServiceStatus.setOnClickListener {
            if (viewModel.isAccessibilityServiceEnabled(this)) {
                Log.d(TAG, "Navigating to accessibility settings to disable service")
                // Desabilitar serviço
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else {
                Log.d(TAG, "Requesting accessibility permission")
                // Habilitar serviço
                requestAccessibilityPermission()
            }
        }

        binding.btnBatteryOptimization.setOnClickListener {
            Log.d(TAG, "Opening battery optimization settings")
            val intent = Intent().apply {
                action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            }
            startActivity(intent)
        }

        binding.btnAutostart.setOnClickListener {
            Log.d(TAG, "Opening autostart settings")
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
                Log.e(TAG, "Failed to open MIUI autostart settings: ${e.message}")
                // Fallback para configurações normais
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
        
        binding.btnSync.setOnClickListener {
            performSync()
        }
    }
    
    private fun performSync() {
        Log.d(TAG, "Syncing data from web")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Show syncing status
                binding.tvSyncStatus.text = "Sincronizando dados..."
                binding.btnSync.isEnabled = false
                
                val result = viewModel.syncAllDataFromWeb()
                
                result.onSuccess {
                    Log.d(TAG, "Sync from web successful")
                    binding.tvSyncStatus.text = "Última sincronização: Agora mesmo"
                    Toast.makeText(
                        this@MainActivity, 
                        "Dados sincronizados com sucesso",
                        Toast.LENGTH_SHORT
                    ).show()
                }.onFailure { error ->
                    Log.e(TAG, "Sync failed: ${error.message}")
                    binding.tvSyncStatus.text = "Erro: ${error.message}"
                    
                    val errorMessage = when {
                        error is UnknownHostException -> 
                            "Erro ao conectar com o servidor. Verifique sua conexão com a internet."
                        error is java.net.ConnectException ->
                            "Não foi possível conectar ao servidor. Verifique se o servidor está ativo."
                        error.message?.contains("timeout", ignoreCase = true) == true ->
                            "A conexão com o servidor expirou. Tente novamente mais tarde."
                        else -> "Erro: ${error.message}"
                    }
                    
                    Toast.makeText(
                        this@MainActivity, 
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync: ${e.message}")
                binding.tvSyncStatus.text = "Erro na sincronização"
                Toast.makeText(
                    this@MainActivity, 
                    "Erro: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.btnSync.isEnabled = true
            }
        }
    }

    private fun checkPermissions() {
        // Verificar permissão de overlay
        if (!Settings.canDrawOverlays(this)) {
            Log.d(TAG, "Requesting overlay permission")
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
        Log.d(TAG, "Service status: ${if (isServiceEnabled) "Active" else "Inactive"}")

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
            Log.d(TAG, "Starting Foccuss service")
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