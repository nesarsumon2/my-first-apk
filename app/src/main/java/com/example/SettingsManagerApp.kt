package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.AutomationRepository
import com.example.util.AutomationScheduler
import com.example.util.NotificationHelper

class SettingsManagerApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AutomationRepository(database.automationDao(), database.automationLogDao()) }
    val scheduler by lazy { AutomationScheduler(this, repository) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
