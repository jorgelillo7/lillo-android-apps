package com.jorgelillo.slipper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.jorgelillo.core.designsystem.installLilloWindow
import com.jorgelillo.core.designsystem.setLilloContent
import com.jorgelillo.core.platform.sharedText
import com.jorgelillo.slipper.ui.SlipperApp
import com.jorgelillo.slipper.ui.SlipperViewModel
import com.jorgelillo.slipper.ui.theme.KitchenColorScheme

class MainActivity : ComponentActivity() {

    private val vm: SlipperViewModel by viewModels {
        SlipperViewModel.factory((application as SlipperApplication).store)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installLilloWindow(darkDesign = false)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleLink(intent)
        setLilloContent(KitchenColorScheme) {
            SlipperApp(vm)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleLink(intent)
    }

    /** A challenge link opened from the browser or a chat, or text shared to the app. */
    private fun handleLink(intent: Intent?) {
        intent.sharedText()?.let(vm::importText)
    }
}
