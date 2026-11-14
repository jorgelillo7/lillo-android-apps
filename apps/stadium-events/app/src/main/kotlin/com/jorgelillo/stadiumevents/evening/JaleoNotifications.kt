package com.jorgelillo.stadiumevents.evening

import android.content.Context
import android.content.Intent
import com.jorgelillo.core.reminders.LilloChannel
import com.jorgelillo.core.reminders.LilloNotificationStyle
import com.jorgelillo.core.reminders.LilloNotifications
import com.jorgelillo.stadiumevents.MainActivity
import com.jorgelillo.stadiumevents.R
import com.jorgelillo.stadiumevents.domain.Reminder
import com.jorgelillo.stadiumevents.ui.JaleoTexts

/**
 * The reminder of a busy day: the evening before, or that morning if the evening said nothing. One
 * channel and one id, so the morning's replaces a stale "tomorrow" and the user can turn both off.
 */
object JaleoNotifications {
    // The id stays "tomorrow" from 1.0: a new id would leave the old channel, and its setting, behind.
    private val channel = LilloChannel("tomorrow", R.string.channel_tomorrow, R.string.channel_tomorrow_desc)
    private val style = LilloNotificationStyle(R.drawable.ic_notification, R.color.notification_accent)
    private const val ID = 1

    fun createChannel(context: Context) = LilloNotifications.createChannels(context, listOf(channel))

    /** True if it was shown (the permission was there). */
    fun show(context: Context, reminder: Reminder): Boolean {
        val texts = JaleoTexts(context.resources)
        return LilloNotifications.show(
            context,
            channel,
            style,
            ID,
            texts.notificationTitle(reminder),
            texts.notification(reminder.day),
            Intent(context, MainActivity::class.java)
        )
    }
}
