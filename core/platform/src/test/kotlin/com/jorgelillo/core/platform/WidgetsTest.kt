package com.jorgelillo.core.platform

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class WidgetsTest {
    class SampleWidget : AppWidgetProvider()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val shadow = shadowOf(AppWidgetManager.getInstance(context))
    private val pins = AppWidgetPins(context, SampleWidget::class.java)

    @Test
    fun aLauncherThatRefusesRequestsIsNotAsked() {
        shadow.setRequestPinAppWidgetSupported(false)
        assertEquals(WidgetOffer(placed = false, pinnable = false), pins.offer())
        assertFalse(pins.requestPin())
    }

    @Test
    fun aLauncherThatAcceptsRequestsIsAsked() {
        shadow.setRequestPinAppWidgetSupported(true)
        assertTrue(pins.offer().pinnable)
        assertTrue(pins.requestPin())
    }

    @Test
    @Config(sdk = [25])
    fun beforeAndroid8NothingIsPinnable() {
        assertEquals(WidgetOffer(placed = false, pinnable = false), pins.offer())
        assertFalse(pins.requestPin())
    }
}
