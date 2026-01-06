package com.example.timemate

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private fun workName(taskId: Int) = "task_reminder_$taskId"

    fun schedule(context: Context, task: TaskEntity) {
        val remindAt = task.remindAt ?: return

        val delayMs = remindAt - System.currentTimeMillis()
        if (delayMs <= 0) return

        val data = Data.Builder()
            .putInt("id", task.id)
            .putString("title", "⏰ ${task.title}")
            .putString("desc", task.desc)
            .build()

        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName(task.id), ExistingWorkPolicy.REPLACE, req)
    }

    fun cancel(context: Context, taskId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(taskId))
    }
}
