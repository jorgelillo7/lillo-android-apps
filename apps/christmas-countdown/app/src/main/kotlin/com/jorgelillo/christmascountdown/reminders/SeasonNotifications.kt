package com.jorgelillo.christmascountdown.reminders

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
import com.jorgelillo.christmascountdown.MainActivity
import com.jorgelillo.christmascountdown.R
import com.jorgelillo.christmascountdown.domain.AdventDoor
import com.jorgelillo.christmascountdown.domain.AdventPlan
import com.jorgelillo.christmascountdown.domain.Reminder
import java.time.LocalDate

/**
 * The notifications themselves. One channel per kind, so people can turn off, say, only the daily
 * door reminder in the system settings.
 */
object SeasonNotifications {
    const val EXTRA_OPEN_ADVENT = "open_advent"

    private enum class Channel(val id: String, val title: Int, val description: Int) {
        COUNTDOWN("countdown", R.string.channel_countdown, R.string.channel_countdown_desc),
        ADVENT("advent", R.string.channel_advent, R.string.channel_advent_desc),
        CHRISTMAS("christmas", R.string.channel_christmas, R.string.channel_christmas_desc)
    }

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        Channel.entries.forEach { channel ->
            val importance = if (channel ==
                Channel.CHRISTMAS
            ) {
                NotificationManager.IMPORTANCE_HIGH
            } else {
                NotificationManager.IMPORTANCE_DEFAULT
            }
            manager.createNotificationChannel(
                NotificationChannel(channel.id, context.getString(channel.title), importance).apply {
                    description = context.getString(channel.description)
                }
            )
        }
    }

    fun canNotify(context: Context): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    fun show(context: Context, reminder: Reminder, date: LocalDate) {
        if (!canNotify(context)) return
        val (channel, title, text) = when (reminder) {
            Reminder.ONE_MONTH_LEFT -> Triple(
                Channel.COUNTDOWN,
                context.getString(R.string.reminder_month_title),
                context.getString(R.string.reminder_month_text)
            )

            Reminder.ONE_WEEK_LEFT -> Triple(
                Channel.COUNTDOWN,
                context.getString(R.string.reminder_week_title),
                context.getString(R.string.reminder_week_text)
            )

            Reminder.ADVENT_STARTS -> Triple(
                Channel.ADVENT,
                context.getString(R.string.reminder_advent_title),
                context.getString(R.string.reminder_advent_text)
            )

            Reminder.DOOR_WAITING -> {
                val music = AdventPlan.forYear(date.year)[date.dayOfMonth] is AdventDoor.Song
                Triple(
                    Channel.ADVENT,
                    context.getString(R.string.reminder_door_title, date.dayOfMonth),
                    context.getString(if (music) R.string.reminder_door_music_text else R.string.reminder_door_text)
                )
            }

            Reminder.CHRISTMAS_DAY -> Triple(
                Channel.CHRISTMAS,
                context.getString(R.string.reminder_christmas_title),
                context.getString(R.string.reminder_christmas_text)
            )
        }
        val opensAdvent = reminder == Reminder.ADVENT_STARTS || reminder == Reminder.DOOR_WAITING
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_OPEN_ADVENT, opensAdvent)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(
            context,
            reminder.ordinal,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(reminder.ordinal, notification)
        } catch (_: SecurityException) {
            // Permission revoked between the check and now: nothing to show.
        }
    }
}
