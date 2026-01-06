package com.example.timemate

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Recordatorio"
        val desc = inputData.getString("desc") ?: ""
        val id = inputData.getInt("id", 0)

        NotificationHelper.show(
            applicationContext,
            notificationId = 100000 + id,
            title = title,
            message = desc
        )

        return Result.success()
    }
}
