package com.example.foccuss.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
                Log.d(TAG, "Changes saved, finishing activity")
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