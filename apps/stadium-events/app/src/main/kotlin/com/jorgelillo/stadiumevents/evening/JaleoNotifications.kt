package com.jorgelillo.stadiumevents.evening

import android.content.Context
import android.content.Intent
import com.jorgelillo.core.reminders.LilloChannel
import com.jorgelillo.core.reminders.LilloNotificationStyle
import com.jorgelillo.core.reminders.LilloNotifications
import com.jorgelillo.stadiumevents.MainActivity
import com.jorgelillo.stadiumevents.R
import com.jorgelillo.stadiumevents.domain.Tomorrow
import com.jorgelillo.stadiumevents.ui.JaleoTexts

/** The reminder the evening before a busy day. One channel, so it can be turned off in the system settings. */
object JaleoNotifications {
    private val tomorrow = LilloChannel("tomorrow", R.string.channel_tomorrow, R.string.channel_tomorrow_desc)
    private val style = LilloNotificationStyle(R.drawable.ic_notification, R.color.notification_accent)
    private const val ID = 1

    fun createChannel(context: Context) = LilloNotifications.createChannels(context, listOf(tomorrow))

    fun show(context: Context, busy: Tomorrow.Busy) {
        val title = context.getString(
            if (busy.maybe) R.string.notification_maybe_title else R.string.notification_title
        )
        LilloNotifications.show(
            context,
            tomorrow,
            style,
            ID,
            title,
            JaleoTexts(context.resources).notification(busy),
            Intent(context, MainActivity::class.java)
        )
    }
}
