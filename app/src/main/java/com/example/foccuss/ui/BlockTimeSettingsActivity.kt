package com.example.foccuss.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.foccuss.data.entity.BlockTimeSettings
import com.example.foccuss.databinding.ActivityBlockTimeSettingsBinding
import com.example.foccuss.viewmodel.BlockTimeSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlockTimeSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockTimeSettingsBinding
    private lateinit var viewModel: BlockTimeSettingsViewModel
    private val TAG = "BlockTimeSettingsActivity"
    
    private var startHour = 8
    private var startMinute = 0
    private var endHour = 17
    private var endMinute = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockTimeSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[BlockTimeSettingsViewModel::class.java]
        
        setupTimeButtons()
        setupSaveButton()
        loadExistingSettings()
    }
    
    private fun setupTimeButtons() {
        binding.btnSetStartTime.setOnClickListener {
            showTimePickerDialog(true)
        }
        
        binding.btnSetEndTime.setOnClickListener {
            showTimePickerDialog(false)
        }
    }
    
    private fun showTimePickerDialog(isStartTime: Boolean) {
        val hour = if (isStartTime) startHour else endHour
        val minute = if (isStartTime) startMinute else endMinute
        
        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                if (isStartTime) {
                    startHour = selectedHour
                    startMinute = selectedMinute
                    updateStartTimeDisplay()
                } else {
                    endHour = selectedHour
                    endMinute = selectedMinute
                    updateEndTimeDisplay()
                }
                Log.d(TAG, "Time set - Start: $startHour:$startMinute, End: $endHour:$endMinute")
            },
            hour,
            minute,
            true // 24 hour format
        ).show()
    }
    
    private fun updateStartTimeDisplay() {
        binding.tvStartTime.text = String.format("%02d:%02d", startHour, startMinute)
    }
    
    private fun updateEndTimeDisplay() {
        binding.tvEndTime.text = String.format("%02d:%02d", endHour, endMinute)
    }
    
    private fun loadExistingSettings() {
        Log.d(TAG, "Loading existing time settings")
        
        viewModel.settings.observe(this) { settings ->
            if (settings != null) {
                Log.d(TAG, "Found existing settings: $settings")
                
                // Set times
                startHour = settings.startHour
                startMinute = settings.startMinute
                endHour = settings.endHour
                endMinute = settings.endMinute
                updateStartTimeDisplay()
                updateEndTimeDisplay()
                
                // Set days of week
                binding.cbMonday.isChecked = settings.monday
                binding.cbTuesday.isChecked = settings.tuesday
                binding.cbWednesday.isChecked = settings.wednesday
                binding.cbThursday.isChecked = settings.thursday
                binding.cbFriday.isChecked = settings.friday
                binding.cbSaturday.isChecked = settings.saturday
                binding.cbSunday.isChecked = settings.sunday
                
                // Set active state
                binding.switchActive.isChecked = settings.isActive
            } else {
                Log.d(TAG, "No existing settings, using defaults")
                updateStartTimeDisplay()
                updateEndTimeDisplay()
                
                // Default weekdays to checked
                binding.cbMonday.isChecked = true
                binding.cbTuesday.isChecked = true
                binding.cbWednesday.isChecked = true
                binding.cbThursday.isChecked = true
                binding.cbFriday.isChecked = true
            }
        }
    }
    
    private fun setupSaveButton() {
        binding.btnSaveTimeSettings.setOnClickListener {
            saveSettings()
        }
    }
    
    private fun saveSettings() {
        Log.d(TAG, "Saving time settings")
        
        val settings = BlockTimeSettings(
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            monday = binding.cbMonday.isChecked,
            tuesday = binding.cbTuesday.isChecked,
            wednesday = binding.cbWednesday.isChecked,
            thursday = binding.cbThursday.isChecked,
            friday = binding.cbFriday.isChecked,
            saturday = binding.cbSaturday.isChecked,
            sunday = binding.cbSunday.isChecked,
            isActive = binding.switchActive.isChecked
        )
        
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.saveSettings(settings)
            Log.d(TAG, "Settings saved, finishing activity")
            finish()
        }
    }
} 