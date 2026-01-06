package com.example.timemate

import android.content.Context

object TaskStore {

    private fun dao(context: Context) =
        TaskDatabase.getInstance(context).taskDao()

    fun add(context: Context, title: String, desc: String, remindAt: Long?) {
        val newId = dao(context)
            .insert(TaskEntity(title = title, desc = desc, remindAt = remindAt))
            .toInt()

        if (remindAt != null) {
            val task = dao(context).getById(newId) ?: return
            ReminderScheduler.schedule(context, task)
        }
    }

    fun getAll(context: Context): List<TaskEntity> {
        return dao(context).getAll()
    }

    fun getById(context: Context, id: Int): TaskEntity? {
        return dao(context).getById(id)
    }

    fun update(context: Context, id: Int, title: String, desc: String, remindAt: Long?) {
        val old = dao(context).getById(id) ?: return

        // cancelar recordatorio anterior
        ReminderScheduler.cancel(context, id)

        val updated = old.copy(title = title, desc = desc, remindAt = remindAt)
        dao(context).update(updated)

        // programar nuevo si existe
        if (remindAt != null) {
            ReminderScheduler.schedule(context, updated)
        }
    }

    fun delete(context: Context, id: Int) {
        val task = dao(context).getById(id) ?: return

        // cancelar recordatorio
        ReminderScheduler.cancel(context, id)

        dao(context).delete(task)
    }

    // ✅ DESHACER BORRADO
    fun restore(context: Context, task: TaskEntity) {
        val newId = dao(context).insert(task.copy(id = 0)).toInt()

        // si tenía recordatorio, reprogramarlo con un nuevo id
        if (task.remindAt != null) {
            val saved = dao(context).getById(newId) ?: return
            ReminderScheduler.schedule(context, saved)
        }
    }
}
