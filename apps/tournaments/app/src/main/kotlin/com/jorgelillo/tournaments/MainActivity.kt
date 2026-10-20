package com.jorgelillo.tournaments

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.jorgelillo.core.designsystem.installLilloWindow
import com.jorgelillo.core.designsystem.setLilloContent
import com.jorgelillo.core.platform.sharedText
import com.jorgelillo.tournaments.ui.TournamentsApp
import com.jorgelillo.tournaments.ui.TournamentsViewModel
import com.jorgelillo.tournaments.ui.theme.ArenaColorScheme

class MainActivity : ComponentActivity() {

    private val vm: TournamentsViewModel by viewModels {
        TournamentsViewModel.factory((application as TournamentsApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installLilloWindow(darkDesign = true)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleLink(intent)
        setLilloContent(ArenaColorScheme) {
            TournamentsApp(vm)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleLink(intent)
    }

    /** A tournament link opened from the browser or a chat, or text shared to the app. */
    private fun handleLink(intent: Intent?) {
        intent.sharedText()?.let(vm::importText)
    }
}
