package com.jorgelillo.decisionwheel

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.jorgelillo.core.designsystem.installLilloWindow
import com.jorgelillo.core.designsystem.setLilloContent
import com.jorgelillo.core.designsystem.startsDark
import com.jorgelillo.decisionwheel.data.Shortcuts
import com.jorgelillo.decisionwheel.ui.DecisionWheelApp
import com.jorgelillo.decisionwheel.ui.theme.WheelDark
import com.jorgelillo.decisionwheel.ui.theme.WheelLight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installLilloWindow(darkDesign = startsDark())
        super.onCreate(savedInstanceState)
        val app = application as DecisionWheelApplication
        setLilloContent(WheelLight, WheelDark) {
            DecisionWheelApp(app, openWheelId = intent?.getStringExtra(Shortcuts.EXTRA_WHEEL_ID))
        }
    }
}
