package com.fbmanager.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fbmanager.R

object NotificationHelper {

    const val CHANNEL_NEW_DEVICES = "fbm_new_devices"

    /** Creates the notification channel. Call once from Application.onCreate(). */
    fun createChannels(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_NEW_DEVICES,
            "Nuevos dispositivos",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alerta cuando un nuevo dispositivo se registra en Firebase"
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    /** Shows a notification for a newly registered device. No-op if permission not granted. */
    fun notifyNewDevice(context: Context, deviceId: String, model: String?, notifId: Int) {
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val title = "Nuevo dispositivo registrado"
        val body = if (!model.isNullOrBlank()) model else deviceId.take(16) + "…"

        val notification = NotificationCompat.Builder(context, CHANNEL_NEW_DEVICES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
    }
}
