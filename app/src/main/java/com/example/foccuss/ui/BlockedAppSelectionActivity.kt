package com.example.foccuss.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foccuss.adapter.AppAdapter
import com.example.foccuss.data.entity.BlockedApp
import com.example.foccuss.databinding.ActivityBlockedAppSelectionBinding
import com.example.foccuss.viewmodel.BlockedAppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlockedAppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockedAppSelectionBinding
    private lateinit var viewModel: BlockedAppViewModel
    private lateinit var adapter: AppAdapter
    private val TAG = "BlockedAppSelection"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[BlockedAppViewModel::class.java]

        setupRecyclerView()
        setupSaveButton()
        loadInstalledApps()
    }

    private fun setupRecyclerView() {
        adapter = AppAdapter { app, isChecked ->
            Log.d(TAG, "App ${app.packageName} toggled: $isChecked")
            CoroutineScope(Dispatchers.Main).launch {
                if (isChecked) {
                    viewModel.addBlockedApp(app)
                } else {
                    viewModel.removeBlockedApp(app)
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BlockedAppSelectionActivity)
            adapter = this@BlockedAppSelectionActivity.adapter
        }

        viewModel.blockedApps.observe(this) { blockedApps ->
            Log.d(TAG, "Observed ${blockedApps.size} blocked apps")
            adapter.updateBlockedApps(blockedApps.associateBy { it.packageName })
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.saveChanges()
                
                // Export blocked apps to web API
                try {
                    val result = viewModel.exportBlockedApps()
                    result.onSuccess { response ->
                        Log.d(TAG, "Export successful: ${response.message}")
                        Toast.makeText(
                            this@BlockedAppSelectionActivity,
                            "Aplicativos salvos e exportados com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.onFailure { error ->
                        Log.e(TAG, "Export failed: ${error.message}")
                        val errorMessage = when {
                            error is java.net.UnknownHostException -> 
                                "Erro ao conectar com o servidor. Verifique a URL da API e sua conexão com a internet."
                            error is java.net.ConnectException ->
                                "Não foi possível conectar ao servidor. Verifique se o servidor está ativo."
                            error.message?.contains("timeout", ignoreCase = true) == true ->
                                "A conexão com o servidor expirou. Tente novamente mais tarde."
                            else -> "Erro: ${error.message}"
                        }
                        Toast.makeText(
                            this@BlockedAppSelectionActivity,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Export exception: ${e.message}")
                    Toast.makeText(
                        this@BlockedAppSelectionActivity,
                        "Aplicativos salvos localmente. Erro ao exportar.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                Log.d(TAG, "Changes saved and exported, finishing activity")
                finish()
            }
        }
    }

    private fun loadInstalledApps() {
        Log.d(TAG, "Loading installed apps")
        CoroutineScope(Dispatchers.Main).launch {
            val apps = withContext(Dispatchers.IO) {
                val pm = packageManager
                val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

                installedApps
                    .filter { app -> app.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                    .map { app ->
                        BlockedApp(
                            packageName = app.packageName,
                            appName = pm.getApplicationLabel(app).toString(),
                            isBlocked = false
                        )
                    }
                    .sortedBy { it.appName }
            }

            Log.d(TAG, "Loaded ${apps.size} user apps")
            adapter.submitList(apps)
        }
    }
}