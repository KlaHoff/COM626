package com.example.mad3d.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mad3d.R
import com.example.mad3d.data.Poi

object NotificationUtils {

    private val notificationCooldown = 10 * 60 * 1000 // 10 minutes
    private val lastNotificationTimestamps = mutableMapOf<Long, Long>()

    fun sendProximityNotification(context: Context, poi: Poi) {
        val currentTime = System.currentTimeMillis()

        // Check if the last notification was sent more than the cooldown period ago
        val lastNotified = lastNotificationTimestamps[poi.osmId] ?: 0
        if (currentTime - lastNotified > notificationCooldown) {
            val channelId = "POI_NOTIFICATION_CHANNEL"
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "POI Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle("Nearby POI")
                .setContentText("You are near ${poi.name}")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            notificationManager.notify(poi.osmId.toInt(), notification)

            // Updating the last notification timestamp
            lastNotificationTimestamps[poi.osmId] = currentTime
        }
    }
}
