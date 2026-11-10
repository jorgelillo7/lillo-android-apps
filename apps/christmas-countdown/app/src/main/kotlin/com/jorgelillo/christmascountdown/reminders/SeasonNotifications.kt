package com.jorgelillo.christmascountdown.reminders

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.jorgelillo.christmascountdown.MainActivity
import com.jorgelillo.christmascountdown.R
import com.jorgelillo.christmascountdown.domain.AdventDoor
import com.jorgelillo.christmascountdown.domain.AdventPlan
import com.jorgelillo.christmascountdown.domain.Reminder
import com.jorgelillo.core.reminders.LilloChannel
import com.jorgelillo.core.reminders.LilloNotificationStyle
import com.jorgelillo.core.reminders.LilloNotifications
import java.time.LocalDate

/**
 * The notifications themselves. One channel per kind, so people can turn off, say, only the daily
 * door reminder in the system settings.
 */
object SeasonNotifications {
    const val EXTRA_OPEN_ADVENT = "open_advent"

    private val countdown = LilloChannel("countdown", R.string.channel_countdown, R.string.channel_countdown_desc)
    private val advent = LilloChannel("advent", R.string.channel_advent, R.string.channel_advent_desc)
    private val christmas = LilloChannel(
        "christmas",
        R.string.channel_christmas,
        R.string.channel_christmas_desc,
        NotificationManager.IMPORTANCE_HIGH
    )
    private val style = LilloNotificationStyle(R.drawable.ic_notification, R.color.notification_accent)

    fun createChannels(context: Context) =
        LilloNotifications.createChannels(context, listOf(countdown, advent, christmas))

    fun show(context: Context, reminder: Reminder, date: LocalDate) {
        val (channel, title, text) = when (reminder) {
            Reminder.ONE_MONTH_LEFT -> Triple(
                countdown,
                context.getString(R.string.reminder_month_title),
                context.getString(R.string.reminder_month_text)
            )

            Reminder.ONE_WEEK_LEFT -> Triple(
                countdown,
                context.getString(R.string.reminder_week_title),
                context.getString(R.string.reminder_week_text)
            )

            Reminder.ADVENT_STARTS -> Triple(
                advent,
                context.getString(R.string.reminder_advent_title),
                context.getString(R.string.reminder_advent_text)
            )

            Reminder.DOOR_WAITING -> {
                val music = AdventPlan.forYear(date.year)[date.dayOfMonth] is AdventDoor.Song
                Triple(
                    advent,
                    context.getString(R.string.reminder_door_title, date.dayOfMonth),
                    context.getString(if (music) R.string.reminder_door_music_text else R.string.reminder_door_text)
                )
            }

            Reminder.CHRISTMAS_DAY -> Triple(
                christmas,
                context.getString(R.string.reminder_christmas_title),
                context.getString(R.string.reminder_christmas_text)
            )
        }
        val opensAdvent = reminder == Reminder.ADVENT_STARTS || reminder == Reminder.DOOR_WAITING
        LilloNotifications.show(
            context,
            channel,
            style,
            reminder.ordinal,
            title,
            text,
            Intent(context, MainActivity::class.java).putExtra(EXTRA_OPEN_ADVENT, opensAdvent)
        )
    }
}
