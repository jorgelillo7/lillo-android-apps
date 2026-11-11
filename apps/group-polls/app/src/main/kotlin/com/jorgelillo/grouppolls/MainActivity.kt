package com.jorgelillo.grouppolls

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.jorgelillo.core.designsystem.installLilloWindow
import com.jorgelillo.core.designsystem.setLilloContent
import com.jorgelillo.grouppolls.ui.GroupPollsApp
import com.jorgelillo.grouppolls.ui.PollsViewModel
import com.jorgelillo.grouppolls.ui.theme.PollsColorScheme

class MainActivity : ComponentActivity() {

    private val vm: PollsViewModel by viewModels {
        val app = application as GroupPollsApplication
        PollsViewModel.factory(app.repository, app.store, resources.configuration.locales[0].language)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installLilloWindow(darkDesign = false)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleLink(intent)
        setLilloContent(PollsColorScheme) {
            GroupPollsApp(vm)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleLink(intent)
    }

    /** `https://jorgelillo7.github.io/q/#CODE` (or the landing page's `…/q/?c=CODE`) opens that poll. */
    private fun handleLink(intent: Intent?) {
        intent?.dataString?.let(vm::open)
    }
}
