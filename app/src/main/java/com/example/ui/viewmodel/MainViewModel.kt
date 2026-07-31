package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SettingsManagerApp
import com.example.data.model.AutomationLog
import com.example.data.model.AutomationTask
import com.example.data.model.TargetAction
import com.example.util.HardwareController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SettingsManagerApp
    private val repository = app.repository
    private val scheduler = app.scheduler
    private val hardwareController = HardwareController(application)

    val tasks: StateFlow<List<AutomationTask>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val logs: StateFlow<List<AutomationLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _wifiState = MutableStateFlow(hardwareController.isWifiEnabled())
    val wifiState: StateFlow<Boolean> = _wifiState.asStateFlow()

    private val _bluetoothState = MutableStateFlow(hardwareController.isBluetoothEnabled())
    val bluetoothState: StateFlow<Boolean> = _bluetoothState.asStateFlow()

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    private val _uiEventMessage = MutableStateFlow<String?>(null)
    val uiEventMessage: StateFlow<String?> = _uiEventMessage.asStateFlow()

    init {
        // Ticker loop for current time (updates countdowns) and hardware state polls
        viewModelScope.launch {
            while (true) {
                _currentTimeMillis.value = System.currentTimeMillis()
                _wifiState.value = hardwareController.isWifiEnabled()
                _bluetoothState.value = hardwareController.isBluetoothEnabled()
                delay(1000)
            }
        }
    }

    fun clearUiMessage() {
        _uiEventMessage.value = null
    }

    fun refreshHardwareStates() {
        _wifiState.value = hardwareController.isWifiEnabled()
        _bluetoothState.value = hardwareController.isBluetoothEnabled()
    }

    fun toggleWifiDirectly() {
        viewModelScope.launch {
            val newState = !_wifiState.value
            val result = hardwareController.setWifiEnabled(newState)
            handleToggleResult("Wi-Fi", result)
            _wifiState.value = hardwareController.isWifiEnabled()
        }
    }

    fun toggleBluetoothDirectly() {
        viewModelScope.launch {
            val newState = !_bluetoothState.value
            val result = hardwareController.setBluetoothEnabled(newState)
            handleToggleResult("Bluetooth", result)
            _bluetoothState.value = hardwareController.isBluetoothEnabled()
        }
    }

    private fun handleToggleResult(type: String, result: HardwareController.ToggleResult) {
        when (result) {
            is HardwareController.ToggleResult.Success -> {
                _uiEventMessage.value = result.message
            }
            is HardwareController.ToggleResult.Error -> {
                _uiEventMessage.value = result.message
            }
            is HardwareController.ToggleResult.RequiresSystemPanel -> {
                _uiEventMessage.value = "$type: ${result.explanation}"
                try {
                    getApplication<Application>().startActivity(result.intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun toggleTaskEnabled(task: AutomationTask) {
        viewModelScope.launch {
            val updated = task.copy(isEnabled = !task.isEnabled)
            repository.updateTask(updated)
            if (updated.isEnabled) {
                scheduler.scheduleTask(updated)
                _uiEventMessage.value = "\"${updated.title}\" enabled and scheduled"
            } else {
                scheduler.cancelTask(updated.id)
                _uiEventMessage.value = "\"${updated.title}\" disabled"
            }
        }
    }

    fun toggleTaskPaused(task: AutomationTask) {
        viewModelScope.launch {
            val updated = task.copy(isPaused = !task.isPaused)
            repository.updateTask(updated)
            if (updated.isPaused) {
                scheduler.cancelTask(updated.id)
                _uiEventMessage.value = "\"${updated.title}\" paused"
            } else {
                scheduler.scheduleTask(updated)
                _uiEventMessage.value = "\"${updated.title}\" resumed"
            }
        }
    }

    fun saveTask(task: AutomationTask) {
        viewModelScope.launch {
            val id = repository.insertTask(task)
            val savedTask = task.copy(id = if (task.id == 0L) id else task.id)
            if (savedTask.isEnabled && !savedTask.isPaused) {
                scheduler.scheduleTask(savedTask)
            }
            _uiEventMessage.value = "Automation saved"
        }
    }

    fun deleteTask(task: AutomationTask) {
        viewModelScope.launch {
            scheduler.cancelTask(task.id)
            repository.deleteTask(task)
            _uiEventMessage.value = "\"${task.title}\" deleted"
        }
    }

    fun triggerTaskNow(task: AutomationTask) {
        viewModelScope.launch {
            val targetState = when (task.targetAction) {
                TargetAction.TURN_OFF -> false
                TargetAction.TURN_ON -> true
                TargetAction.TOGGLE -> {
                    if (task.type.isWifi()) !_wifiState.value else !_bluetoothState.value
                }
            }
            val result = if (task.type.isWifi()) {
                hardwareController.setWifiEnabled(targetState)
            } else {
                hardwareController.setBluetoothEnabled(targetState)
            }
            handleToggleResult(if (task.type.isWifi()) "Wi-Fi" else "Bluetooth", result)
            
            // Record manual execution log
            repository.addLog(
                AutomationLog(
                    taskId = task.id,
                    taskTitle = task.title,
                    actionText = "Manual Trigger (${if (task.type.isWifi()) "Wi-Fi" else "Bluetooth"})",
                    success = result is HardwareController.ToggleResult.Success,
                    detailMessage = "Triggered manually from app",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            _uiEventMessage.value = "Logs cleared"
        }
    }
}
