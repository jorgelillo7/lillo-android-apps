package com.jorgelillo.stadiumevents

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.jorgelillo.core.designsystem.installLilloWindow
import com.jorgelillo.core.designsystem.setLilloContent
import com.jorgelillo.stadiumevents.ui.JaleoApp
import com.jorgelillo.stadiumevents.ui.theme.DarkPalette
import com.jorgelillo.stadiumevents.ui.theme.JaleoDarkScheme
import com.jorgelillo.stadiumevents.ui.theme.JaleoLightScheme
import com.jorgelillo.stadiumevents.ui.theme.LightPalette

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Light unless the phone is in dark mode (switching recreates the activity).
        val dark =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        installLilloWindow(darkDesign = dark)
        super.onCreate(savedInstanceState)
        val app = application as StadiumEventsApplication
        setLilloContent(if (dark) JaleoDarkScheme else JaleoLightScheme) {
            JaleoApp(app, if (dark) DarkPalette else LightPalette)
        }
    }
}
