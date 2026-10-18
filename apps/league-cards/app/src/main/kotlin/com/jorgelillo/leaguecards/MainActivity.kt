package com.jorgelillo.leaguecards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.jorgelillo.core.designsystem.installLilloWindow
import com.jorgelillo.core.designsystem.setLilloContent
import com.jorgelillo.leaguecards.ui.LeagueApp
import com.jorgelillo.leaguecards.ui.LeagueViewModel
import com.jorgelillo.leaguecards.ui.theme.ChatColorScheme

class MainActivity : ComponentActivity() {

    private val vm: LeagueViewModel by viewModels {
        val app = application as LeagueCardsApplication
        LeagueViewModel.factory(app.league, app.store)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installLilloWindow(darkDesign = true)
        super.onCreate(savedInstanceState)
        setLilloContent(ChatColorScheme) { LeagueApp(vm) }
    }
}
