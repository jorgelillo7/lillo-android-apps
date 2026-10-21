package com.jorgelillo.christmascountdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.jorgelillo.christmascountdown.reminders.SeasonNotifications
import com.jorgelillo.christmascountdown.ui.ChristmasApp
import com.jorgelillo.christmascountdown.ui.theme.ChristmasColorScheme
import com.jorgelillo.core.designsystem.installLilloWindow
import com.jorgelillo.core.designsystem.setLilloContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installLilloWindow(darkDesign = true)
        super.onCreate(savedInstanceState)
        val app = application as ChristmasCountdownApplication
        setLilloContent(ChristmasColorScheme) {
            ChristmasApp(
                app,
                openAdvent = intent?.getBooleanExtra(SeasonNotifications.EXTRA_OPEN_ADVENT, false) == true
            )
        }
    }
}
