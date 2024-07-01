package com.example.mad3d.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mad3d.R
import com.example.mad3d.data.Poi

// Utility object for sending notifications about POIs
object NotificationUtils {

    // cooldown period for sending notifications in milliseconds (10 minutes)
    private val notificationCooldown = 10 * 60 * 1000
    // map to track the last notification timestamp for each POI by its osmId
    private val lastNotificationTimestamps = mutableMapOf<Long, Long>()

    // function to send a proximity notification for a POI
    fun sendProximityNotification(context: Context, poi: Poi) {
        val currentTime = System.currentTimeMillis()

        // get the last notification timestamp for this POI or default to 0
        val lastNotified = lastNotificationTimestamps[poi.osmId] ?: 0
        // check if the cooldown period has passed since the last notification
        if (currentTime - lastNotified > notificationCooldown) {
            val channelId = "POI_NOTIFICATION_CHANNEL"
            // get the notification manager system service
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // create a notification channel if the Android version is Oreo or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "POI Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            // build the notification
            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle("Nearby POI")
                .setContentText("You are near ${poi.name}")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            // send the notification
            notificationManager.notify(poi.osmId.toInt(), notification)

            // update the last notification timestamp for this POI
            lastNotificationTimestamps[poi.osmId] = currentTime
        }
    }
}
