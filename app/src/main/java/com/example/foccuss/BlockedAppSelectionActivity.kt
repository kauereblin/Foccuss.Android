package com.example.foccuss.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[BlockedAppViewModel::class.java]

        setupRecyclerView()
        loadInstalledApps()
    }

    private fun setupRecyclerView() {
        adapter = AppAdapter { app, isChecked ->
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

        // Observar mudanças na lista de apps bloqueados
        viewModel.blockedApps.observe(this) { blockedApps ->
            adapter.updateBlockedApps(blockedApps.associateBy { it.packageName })
        }
    }

    private fun loadInstalledApps() {
        CoroutineScope(Dispatchers.Main).launch {
            val apps = withContext(Dispatchers.IO) {
                val pm = packageManager
                val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

                // Filtrar apenas aplicativos de usuário (não sistema)
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

            adapter.submitList(apps)
        }
    }
}