package com.example.foccuss.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foccuss.data.entity.BlockedApp
import com.example.foccuss.databinding.ItemAppBinding

class AppAdapter(
    private val onAppToggled: (BlockedApp, Boolean) -> Unit
) : ListAdapter<BlockedApp, AppAdapter.AppViewHolder>(AppDiffCallback()) {

    private var blockedAppsMap: Map<String, BlockedApp> = emptyMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = getItem(position)
        val isBlocked = blockedAppsMap.containsKey(app.packageName)
        holder.bind(app, isBlocked)
    }

    fun updateBlockedApps(blockedApps: Map<String, BlockedApp>) {
        blockedAppsMap = blockedApps
        val list = currentList.map { app ->
            app.copy(isBlocked = blockedApps.containsKey(app.packageName))
        }
        submitList(list)
    }

    inner class AppViewHolder(private val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(app: BlockedApp, isBlocked: Boolean) {
            binding.tvAppName.text = app.appName
            binding.tvPackageName.text = app.packageName
            binding.checkBox.isChecked = isBlocked

            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                onAppToggled(app, isChecked)
            }

            binding.root.setOnClickListener {
                binding.checkBox.toggle()
            }
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<BlockedApp>() {
        override fun areItemsTheSame(oldItem: BlockedApp, newItem: BlockedApp): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: BlockedApp, newItem: BlockedApp): Boolean {
            return oldItem == newItem
        }
    }
}