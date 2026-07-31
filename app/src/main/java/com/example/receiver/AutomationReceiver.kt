package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.SettingsManagerApp
import com.example.data.model.AutomationLog
import com.example.data.model.TargetAction
import com.example.util.HardwareController
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AutomationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val app = context.applicationContext as? SettingsManagerApp ?: return
        val repository = app.repository
        val scheduler = app.scheduler
        val hardwareController = HardwareController(context)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = repository.getTaskById(taskId)
                if (task == null || !task.isEnabled || task.isPaused) {
                    pendingResult.finish()
                    return@launch
                }

                val targetState = when (task.targetAction) {
                    TargetAction.TURN_OFF -> false
                    TargetAction.TURN_ON -> true
                    TargetAction.TOGGLE -> {
                        if (task.type.isWifi()) !hardwareController.isWifiEnabled()
                        else !hardwareController.isBluetoothEnabled()
                    }
                }

                val toggleResult = if (task.type.isWifi()) {
                    hardwareController.setWifiEnabled(targetState)
                } else {
                    hardwareController.setBluetoothEnabled(targetState)
                }

                val now = System.currentTimeMillis()
                val isSuccess: Boolean
                val detailMsg: String
                var actionIntent: Intent? = null
                var actionLabel: String? = null

                when (toggleResult) {
                    is HardwareController.ToggleResult.Success -> {
                        isSuccess = true
                        detailMsg = toggleResult.message
                    }
                    is HardwareController.ToggleResult.Error -> {
                        isSuccess = false
                        detailMsg = toggleResult.message
                    }
                    is HardwareController.ToggleResult.RequiresSystemPanel -> {
                        isSuccess = false
                        detailMsg = toggleResult.explanation
                        actionIntent = toggleResult.intent
                        actionLabel = "Open Panel"
                    }
                }

                val hardwareName = if (task.type.isWifi()) "Wi-Fi" else "Bluetooth"
                val stateText = if (targetState) "ON" else "OFF"
                val notifTitle = "$hardwareName Automated $stateText"
                val notifMsg = if (isSuccess) {
                    "$hardwareName has been automatically turned $stateText by \"${task.title}\"."
                } else {
                    "Automation \"${task.title}\" triggered: $detailMsg"
                }

                NotificationHelper.showNotification(
                    context = context,
                    notificationId = taskId.toInt(),
                    title = notifTitle,
                    message = notifMsg,
                    actionIntent = actionIntent,
                    actionLabel = actionLabel
                )

                // Log execution
                repository.addLog(
                    AutomationLog(
                        taskId = task.id,
                        taskTitle = task.title,
                        actionText = "$hardwareName Turn $stateText",
                        success = isSuccess,
                        detailMessage = detailMsg,
                        timestamp = now
                    )
                )

                if (task.repeatDaily && task.type.isSchedule()) {
                    // Re-schedule for tomorrow
                    val updatedTask = task.copy(
                        lastTriggeredAt = now,
                        triggerTimeMillis = 0L // Scheduler will compute next day's time
                    )
                    repository.updateTask(updatedTask)
                    scheduler.scheduleTask(updatedTask)
                } else {
                    // One-shot timer or non-repeating schedule complete
                    val updatedTask = task.copy(
                        isEnabled = false,
                        lastTriggeredAt = now
                    )
                    repository.updateTask(updatedTask)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_automation_task_id"
    }
}
