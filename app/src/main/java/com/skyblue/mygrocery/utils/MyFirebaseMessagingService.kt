package com.skyblue.mygrocery.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.ui.activity.TrackOrderActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 1. Extract data from the notification payload
        val title = remoteMessage.notification?.title ?: "Order Update"
        val message = remoteMessage.notification?.body ?: "Your order status has changed."

        // 2. You can also send custom data (like orderId) to open a specific screen
        val orderId = remoteMessage.data["orderId"]

        showNotification(title, message, orderId)
    }

    private fun showNotification(title: String, message: String, orderId: String?) {
        val channelId = "order_updates_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Order Updates", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open TrackOrderActivity when notification is clicked
        val intent = Intent(this, TrackOrderActivity::class.java).apply {
            putExtra("order_id", orderId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app logo
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send this token to your server so you know which device to send notifications to
        Log.d("FCM_TOKEN", "Refreshed token: $token")
    }
}