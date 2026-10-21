package com.jorgelillo.whoslying

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import com.jorgelillo.core.designsystem.installLilloWindow
import com.jorgelillo.core.designsystem.setLilloContent
import com.jorgelillo.core.platform.sharedText
import com.jorgelillo.whoslying.ui.WhosLyingApp
import com.jorgelillo.whoslying.ui.theme.NeonColorScheme

class MainActivity : ComponentActivity() {

    /** A pack link opened from a chat or the website, or text shared to the app; handled once. */
    private val incoming = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installLilloWindow(darkDesign = true)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleLink(intent)
        val app = application as WhosLyingApplication
        setLilloContent(NeonColorScheme) {
            WhosLyingApp(app, incomingText = incoming.value, onConsumeIncoming = { incoming.value = null })
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleLink(intent)
    }

    private fun handleLink(intent: Intent?) {
        incoming.value = intent.sharedText()
    }
}
