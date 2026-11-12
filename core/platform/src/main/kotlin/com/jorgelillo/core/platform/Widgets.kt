package com.jorgelillo.core.platform

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build

/**
 * What the app can say about its home-screen widget: whether one is already placed, and whether
 * the launcher accepts a request to place it. A launcher that does not (MIUI and others) or one
 * that drops the request without a word still lets the user add it by hand from its widget list,
 * so the app always explains that way too.
 */
data class WidgetOffer(val placed: Boolean, val pinnable: Boolean)

/** The app's widget as the launcher sees it. */
interface WidgetPins {
    fun offer(): WidgetOffer

    /** Asks the launcher to place the widget; false when it can't be asked. */
    fun requestPin(): Boolean
}

/** [WidgetPins] for the widget whose `AppWidgetProvider` (or Glance receiver) is [receiver]. */
class AppWidgetPins(context: Context, receiver: Class<*>) : WidgetPins {
    private val context = context.applicationContext
    private val provider = ComponentName(this.context, receiver)
    private val manager: AppWidgetManager? get() = AppWidgetManager.getInstance(context)

    override fun offer(): WidgetOffer {
        val manager = manager ?: return WidgetOffer(placed = false, pinnable = false)
        return WidgetOffer(
            placed = manager.getAppWidgetIds(provider).isNotEmpty(),
            pinnable = canAskLauncher(manager)
        )
    }

    override fun requestPin(): Boolean {
        val manager = manager ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !manager.isRequestPinAppWidgetSupported) return false
        return runCatching { manager.requestPinAppWidget(provider, null, null) }.getOrDefault(false)
    }

    // Pinning from the app arrived in Android 8; before that, only the launcher's widget list.
    private fun canAskLauncher(manager: AppWidgetManager): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported
}
