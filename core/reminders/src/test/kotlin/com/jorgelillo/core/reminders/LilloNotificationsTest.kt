package com.jorgelillo.core.reminders

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class LilloNotificationsTest {
    private val app: Application = ApplicationProvider.getApplicationContext()
    private val context: Context = app
    private val manager get() = context.getSystemService(NotificationManager::class.java)
    private val channel = LilloChannel("tomorrow", android.R.string.ok, android.R.string.cancel)
    private val style = LilloNotificationStyle(android.R.drawable.ic_dialog_info, android.R.color.black)

    private fun show(id: Int = 1, text: String = "text") =
        LilloNotifications.show(context, channel, style, id, "title", text, Intent(Intent.ACTION_MAIN))

    @Test
    @Config(sdk = [36])
    fun fromAndroid13NotificationsNeedThePermission() {
        assertFalse(LilloNotifications.canNotify(context))
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertTrue(LilloNotifications.canNotify(context))
    }

    @Test
    @Config(sdk = [32])
    fun beforeAndroid13NotificationsAreAllowedWithoutAsking() {
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertTrue(LilloNotifications.canNotify(context))
    }

    @Test
    @Config(sdk = [36])
    fun nothingIsShownWithoutThePermission() {
        show()
        assertEquals(0, shadowOf(manager).size())
    }

    @Test
    @Config(sdk = [36])
    fun aNotificationIsShownOnItsChannelAndANewOneWithTheSameIdReplacesIt() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        show(id = 7, text = "first")
        show(id = 7, text = "second")
        val shown = shadowOf(manager).allNotifications.single()
        assertEquals("tomorrow", shown.channelId)
        assertEquals("second", shown.extras.getCharSequence("android.text").toString())
        show(id = 8)
        assertEquals(2, shadowOf(manager).size())
    }

    @Test
    @Config(sdk = [36])
    fun channelsAreCreatedOnceWhateverTheNumberOfStarts() {
        val other = LilloChannel("other", android.R.string.ok, android.R.string.cancel)
        repeat(3) { LilloNotifications.createChannels(context, listOf(channel, other)) }
        assertEquals(setOf("tomorrow", "other"), manager.notificationChannels.map { it.id }.toSet())
        val created = assertNotNull(manager.getNotificationChannel("tomorrow"))
        assertEquals(context.getString(android.R.string.ok), created.name.toString())
        assertEquals(context.getString(android.R.string.cancel), created.description)
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, created.importance)
    }

    @Test
    @Config(sdk = [25])
    fun beforeAndroid8ThereAreNoChannelsToCreateAndNothingBreaks() {
        // The channel API does not exist here: calling it would throw NoSuchMethodError.
        LilloNotifications.createChannels(context, listOf(channel))
    }
}
