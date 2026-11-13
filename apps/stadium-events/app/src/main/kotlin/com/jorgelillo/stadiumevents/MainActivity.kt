package com.jorgelillo.stadiumevents

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.jorgelillo.core.designsystem.installLilloWindow
import com.jorgelillo.core.designsystem.setLilloContent
import com.jorgelillo.core.designsystem.startsDark
import com.jorgelillo.stadiumevents.ui.JaleoApp
import com.jorgelillo.stadiumevents.ui.theme.JaleoDark
import com.jorgelillo.stadiumevents.ui.theme.JaleoLight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installLilloWindow(darkDesign = startsDark())
        super.onCreate(savedInstanceState)
        val app = application as StadiumEventsApplication
        setLilloContent(JaleoLight, JaleoDark) { JaleoApp(app) }
    }
}
