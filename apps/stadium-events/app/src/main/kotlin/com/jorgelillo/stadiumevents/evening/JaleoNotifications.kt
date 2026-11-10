package com.jorgelillo.stadiumevents.evening

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jorgelillo.stadiumevents.MainActivity
import com.jorgelillo.stadiumevents.R
import com.jorgelillo.stadiumevents.domain.Tomorrow
import com.jorgelillo.stadiumevents.ui.JaleoTexts

/** The reminder the evening before a busy day. One channel, so it can be turned off in the system settings. */
object JaleoNotifications {
    private const val CHANNEL = "tomorrow"
    private const val ID = 1

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL,
            context.getString(R.string.channel_tomorrow),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.channel_tomorrow_desc) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canNotify(context: Context): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    fun show(context: Context, busy: Tomorrow.Busy) {
        if (!canNotify(context)) return
        val texts = JaleoTexts(context.resources)
        val title = context.getString(
            if (busy.maybe) R.string.notification_maybe_title else R.string.notification_title
        )
        val text = texts.notification(busy)
        val pending = PendingIntent.getActivity(
            context,
            0,
            Intent(
                context,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(ID, notification)
        } catch (_: SecurityException) {
            // Permission revoked between the check and now: nothing to show.
        }
    }
}
