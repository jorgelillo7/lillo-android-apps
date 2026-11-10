package com.jorgelillo.core.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** A notification channel: people can turn each one off on its own in the system settings. */
data class LilloChannel(
    val id: String,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
)

/** The look every app's notifications share: its status bar icon and accent colour. */
data class LilloNotificationStyle(@param:DrawableRes val smallIcon: Int, @param:ColorRes val color: Int)

/** Notifications the way every app shows them: channels, the permission check, and a tap that opens the app. */
object LilloNotifications {
    fun createChannels(context: Context, channels: List<LilloChannel>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        channels.forEach { channel ->
            manager.createNotificationChannel(
                NotificationChannel(channel.id, context.getString(channel.title), channel.importance).apply {
                    description = context.getString(channel.description)
                }
            )
        }
    }

    /** Android 13+ asks for permission; before that, notifications are allowed. */
    fun canNotify(context: Context): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    /**
     * Shows [title] and [text] (expanded when long) on [channel]; tapping it starts [open]. A new one
     * with the same [id] replaces the previous. Does nothing without the permission.
     */
    fun show(
        context: Context,
        channel: LilloChannel,
        style: LilloNotificationStyle,
        id: Int,
        title: String,
        text: String,
        open: Intent
    ) {
        if (!canNotify(context)) return
        val pending = PendingIntent.getActivity(
            context,
            id,
            open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(style.smallIcon)
            .setColor(ContextCompat.getColor(context, style.color))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permission revoked between the check and now: nothing to show.
        }
    }
}
