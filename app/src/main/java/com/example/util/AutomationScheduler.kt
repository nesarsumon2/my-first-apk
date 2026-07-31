package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.AutomationTask
import com.example.data.repository.AutomationRepository
import com.example.receiver.AutomationReceiver
import java.util.Calendar

class AutomationScheduler(
    private val context: Context,
    private val repository: AutomationRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    suspend fun scheduleTask(task: AutomationTask) {
        if (!task.isEnabled || task.isPaused) {
            cancelTask(task.id)
            return
        }

        val triggerAtMillis = calculateNextTriggerTime(task)
        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.w("AutomationScheduler", "Trigger time is in the past for task ${task.id}")
            return
        }

        val intent = Intent(context, AutomationReceiver::class.java).apply {
            putExtra(AutomationReceiver.EXTRA_TASK_ID, task.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val updatedTask = task.copy(triggerTimeMillis = triggerAtMillis)
        repository.updateTask(updatedTask)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager?.canScheduleExactAlarms() == true) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager?.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d("AutomationScheduler", "Scheduled task ${task.id} for $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.e("AutomationScheduler", "Exact alarm permission missing", e)
            alarmManager?.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e("AutomationScheduler", "Failed to schedule alarm", e)
        }
    }

    fun cancelTask(taskId: Long) {
        val intent = Intent(context, AutomationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("AutomationScheduler", "Cancelled task $taskId")
        }
    }

    suspend fun rescheduleAllActiveTasks() {
        val activeTasks = repository.getActiveTasksSync()
        for (task in activeTasks) {
            scheduleTask(task)
        }
    }

    companion object {
        fun calculateNextTriggerTime(task: AutomationTask): Long {
            val now = System.currentTimeMillis()

            if (task.type.isTimer()) {
                // For timers, if triggerTimeMillis is set and in the future, keep it; else calculate from duration
                return if (task.triggerTimeMillis > now) {
                    task.triggerTimeMillis
                } else {
                    now + (task.durationMinutes * 60 * 1000L)
                }
            } else {
                // For schedules (daily or one-time specified hour:minute)
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = now
                    set(Calendar.HOUR_OF_DAY, task.hourOfDay)
                    set(Calendar.MINUTE, task.minuteOfHour)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (calendar.timeInMillis <= now) {
                    // If time passed today, move to tomorrow
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                return calendar.timeInMillis
            }
        }
    }
}
