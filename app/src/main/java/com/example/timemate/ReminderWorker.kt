package com.example.timemate

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        // obtenemos los datos enviados desde el scheduler
        val title = inputData.getString("title") ?: "recordatorio"
        val desc = inputData.getString("desc") ?: ""
        val id = inputData.getInt("id", 0)

        // mostramos la notificación cuando llega la hora
        NotificationHelper.show(
            applicationContext,
            notificationId = 100000 + id,
            title = title,
            message = desc
        )

        // indicamos a workmanager que el trabajo terminó correctamente
        return Result.success()
    }
}
