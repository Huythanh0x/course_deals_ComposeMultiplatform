package com.thanh0x.coursedeals.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.thanh0x.coursedeals.MainActivity
import com.thanh0x.coursedeals.core.common.BundleKey
import com.thanh0x.coursedeals.domain.user.DeviceTokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class CourseDealsFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            runCatching {
                val repository = get<DeviceTokenRepository>()
                repository.saveDeviceToken(token)
                repository.registerDeviceTokenRemote(token, "ANDROID")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val courseId = data["courseId"] ?: data["course_id"]
        val deepLink = data["deepLink"] ?: data["deep_link"] ?: courseId?.let { "coursedeals://courses/$it" }

        val title = remoteMessage.notification?.title ?: data["title"] ?: "Course Deal Alert"
        val body = remoteMessage.notification?.body ?: data["body"] ?: "A new course deal is available!"

        showNotification(title = title, body = body, deepLink = deepLink, courseId = courseId?.toIntOrNull())
    }

    private fun showNotification(
        title: String,
        body: String,
        deepLink: String?,
        courseId: Int?,
    ) {
        val channelId = CHANNEL_ID
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Course Deals",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Course deal alerts and discount notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!deepLink.isNullOrBlank()) {
                setData(Uri.parse(deepLink))
            }
            if (courseId != null) {
                putExtra(BundleKey.TO_DETAIL_ACTIVITY, courseId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "course_deals_channel"
    }
}
