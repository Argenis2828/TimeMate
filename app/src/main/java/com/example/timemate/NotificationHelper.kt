package com.example.timemate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    // ID del canal de notificaciones de la app
    const val CHANNEL_ID = "timemate_reminders"

    /**
     * Crea el canal de notificaciones (solo es necesario en Android 8+).
     * Si el canal ya existe, Android simplemente lo ignora.
     */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios TimeMate",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de recordatorios de tareas"
            }

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    /**
     * Muestra una notificación de recordatorio.
     * Al tocarla, el usuario vuelve directamente a la app.
     */
    fun show(context: Context, notificationId: Int, title: String, message: String) {
        // Nos aseguramos de que el canal exista antes de mostrar la notificación
        ensureChannel(context)

        // Intent para abrir la app cuando el usuario toca la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // PendingIntent que Android usará al pulsar la notificación
        val pending = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Construimos la notificación
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)   // Que se muestre arriba y con sonido
            .setAutoCancel(true)                             // Se quita sola al tocarla
            .build()

        // Enviamos la notificación al sistema
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, notif)
    }
}
