package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Update
import com.example.data.model.AutomationLog
import com.example.data.model.AutomationTask
import com.example.data.model.AutomationType
import com.example.data.model.TargetAction
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromAutomationType(type: AutomationType): String = type.name

    @TypeConverter
    fun toAutomationType(value: String): AutomationType = try {
        AutomationType.valueOf(value)
    } catch (e: Exception) {
        AutomationType.WIFI_TIMER
    }

    @TypeConverter
    fun fromTargetAction(action: TargetAction): String = action.name

    @TypeConverter
    fun toTargetAction(value: String): TargetAction = try {
        TargetAction.valueOf(value)
    } catch (e: Exception) {
        TargetAction.TURN_OFF
    }
}

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automation_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<AutomationTask>>

    @Query("SELECT * FROM automation_tasks WHERE isEnabled = 1 ORDER BY createdAt DESC")
    fun getEnabledTasksFlow(): Flow<List<AutomationTask>>

    @Query("SELECT * FROM automation_tasks WHERE isEnabled = 1 AND isPaused = 0")
    suspend fun getActiveTasksSync(): List<AutomationTask>

    @Query("SELECT * FROM automation_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): AutomationTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AutomationTask): Long

    @Update
    suspend fun updateTask(task: AutomationTask)

    @Delete
    suspend fun deleteTask(task: AutomationTask)

    @Query("DELETE FROM automation_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}

@Dao
interface AutomationLogDao {
    @Query("SELECT * FROM automation_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<AutomationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AutomationLog)

    @Query("DELETE FROM automation_logs")
    suspend fun clearLogs()
}
