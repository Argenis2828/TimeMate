package com.example.timemate

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    // genera un nombre único para cada recordatorio según el id de la tarea
    private fun workName(taskId: Int) = "task_reminder_$taskId"

    // programa el recordatorio de una tarea si tiene fecha definida
    fun schedule(context: Context, task: TaskEntity) {

        // si la tarea no tiene recordatorio, no hacemos nada
        val remindAt = task.remindAt ?: return

        // calculamos cuánto tiempo falta para que suene el recordatorio
        val delayMs = remindAt - System.currentTimeMillis()
        if (delayMs <= 0) return

        // datos que se le envían al worker (id, título y descripción)
        val data = Data.Builder()
            .putInt("id", task.id)
            .putString("title", "⏰ ${task.title}")
            .putString("desc", task.desc)
            .build()

        // creamos el worker que se ejecutará cuando llegue la hora
        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        // si ya existe un recordatorio para esta tarea, lo reemplazamos
        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName(task.id), ExistingWorkPolicy.REPLACE, req)
    }

    // cancela el recordatorio de una tarea específica
    fun cancel(context: Context, taskId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(taskId))
    }
}
