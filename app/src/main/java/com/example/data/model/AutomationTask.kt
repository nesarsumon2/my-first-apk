package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_tasks")
data class AutomationTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: AutomationType,
    val targetAction: TargetAction,
    val durationMinutes: Int = 0, // Used for timers (e.g. 30 mins)
    val triggerTimeMillis: Long = 0L, // Used for countdown target timestamp
    val hourOfDay: Int = 0, // 0-23 for daily schedule
    val minuteOfHour: Int = 0, // 0-59 for daily schedule
    val repeatDaily: Boolean = false,
    val isEnabled: Boolean = true,
    val isPaused: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long? = null
)

@Entity(tableName = "automation_logs")
data class AutomationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val taskTitle: String,
    val actionText: String,
    val success: Boolean,
    val detailMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)
