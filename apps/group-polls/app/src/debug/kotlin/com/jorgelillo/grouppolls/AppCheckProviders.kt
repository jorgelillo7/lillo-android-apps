package com.jorgelillo.grouppolls

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Debug builds: the debug provider logs a token to register in the Firebase console
 * (App Check → Apps → Manage debug tokens).
 */
object AppCheckProviders {
    fun factory(): AppCheckProviderFactory = DebugAppCheckProviderFactory.getInstance()
}
