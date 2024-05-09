package com.xpynx.todolist.notification;

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.xpynx.todolist.R


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseMessageReceiver : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            println("hi this is title"+it.title);
            println("hi this is body"+it.body);
            println("hi this is instance"+it);
            val notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_ID")
                .setContentTitle(it.title)
                .setContentText(it.body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.priority_high)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = generateNotificationId()
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }

    private fun generateNotificationId(): Int {
        return System.currentTimeMillis().toInt()
    }
}
