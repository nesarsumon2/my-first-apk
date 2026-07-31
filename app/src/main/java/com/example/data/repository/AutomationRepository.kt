package com.example.data.repository

import com.example.data.db.AutomationDao
import com.example.data.db.AutomationLogDao
import com.example.data.model.AutomationLog
import com.example.data.model.AutomationTask
import kotlinx.coroutines.flow.Flow

class AutomationRepository(
    private val automationDao: AutomationDao,
    private val logDao: AutomationLogDao
) {
    val allTasks: Flow<List<AutomationTask>> = automationDao.getAllTasks()
    val allLogs: Flow<List<AutomationLog>> = logDao.getAllLogs()

    suspend fun getTaskById(id: Long): AutomationTask? = automationDao.getTaskById(id)

    suspend fun getActiveTasksSync(): List<AutomationTask> = automationDao.getActiveTasksSync()

    suspend fun insertTask(task: AutomationTask): Long = automationDao.insertTask(task)

    suspend fun updateTask(task: AutomationTask) = automationDao.updateTask(task)

    suspend fun deleteTask(task: AutomationTask) = automationDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = automationDao.deleteTaskById(id)

    suspend fun addLog(log: AutomationLog) = logDao.insertLog(log)

    suspend fun clearLogs() = logDao.clearLogs()
}
