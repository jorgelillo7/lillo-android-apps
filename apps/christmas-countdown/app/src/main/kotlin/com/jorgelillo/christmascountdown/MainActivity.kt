package com.jorgelillo.christmascountdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.jorgelillo.christmascountdown.reminders.SeasonNotifications
import com.jorgelillo.christmascountdown.ui.ChristmasApp
import com.jorgelillo.christmascountdown.ui.theme.ChristmasDark
import com.jorgelillo.christmascountdown.ui.theme.ChristmasLight
import com.jorgelillo.core.designsystem.installLilloWindow
import com.jorgelillo.core.designsystem.setLilloContent
import com.jorgelillo.core.designsystem.startsDark

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installLilloWindow(darkDesign = startsDark())
        super.onCreate(savedInstanceState)
        val app = application as ChristmasCountdownApplication
        setLilloContent(ChristmasLight, ChristmasDark) {
            ChristmasApp(
                app,
                openAdvent = intent?.getBooleanExtra(SeasonNotifications.EXTRA_OPEN_ADVENT, false) == true
            )
        }
    }
}
