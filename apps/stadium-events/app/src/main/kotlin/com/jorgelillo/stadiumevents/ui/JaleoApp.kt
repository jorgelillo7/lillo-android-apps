package com.jorgelillo.stadiumevents.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorgelillo.core.designsystem.AboutTexts
import com.jorgelillo.core.designsystem.LilloAboutSheet
import com.jorgelillo.core.platform.AppWidgetPins
import com.jorgelillo.core.platform.LilloLinks
import com.jorgelillo.core.reminders.LilloNotifications
import com.jorgelillo.stadiumevents.BuildConfig
import com.jorgelillo.stadiumevents.R
import com.jorgelillo.stadiumevents.StadiumEventsApplication
import com.jorgelillo.stadiumevents.ui.theme.jaleoPalette
import com.jorgelillo.stadiumevents.widget.JaleoWidgetReceiver

/** The screen with its ViewModel, the About sheet, and the one-time notifications permission. */
@Composable
internal fun JaleoApp(app: StadiumEventsApplication) {
    val vm: JaleoViewModel = viewModel(
        factory = JaleoViewModel.factory(app.repository, AppWidgetPins(app, JaleoWidgetReceiver::class.java))
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val widget by vm.widget.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        vm.refreshWidget()
        onPauseOrDispose { }
    }
    var about by rememberSaveable { mutableStateOf(false) }

    // Nothing to set up: the reminder is on from the start, so the permission is asked once, at once.
    val context = LocalContext.current
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        vm.notificationsAsked()
    }
    LaunchedEffect(state?.notificationsAsked) {
        if (state?.notificationsAsked == false) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !LilloNotifications.canNotify(context)) {
                askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                vm.notificationsAsked()
            }
        }
    }

    JaleoScreen(
        state,
        jaleoPalette(),
        onAbout = { about = true },
        onRetry = vm::refresh,
        widget = widget,
        onAddWidget = vm::addWidget
    )
    if (about) AboutSheet(onDismiss = { about = false })
}

@Composable
private fun AboutSheet(onDismiss: () -> Unit) = LilloAboutSheet(
    icon = painterResource(R.drawable.ic_launcher_foreground),
    iconBackground = Color(0xFF1C3350),
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    texts = AboutTexts(
        title = stringResource(R.string.about_title),
        version = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
        body = stringResource(R.string.about_body),
        rate = stringResource(R.string.about_rate),
        privacy = stringResource(R.string.about_privacy)
    ),
    privacyUrl = LilloLinks.privacyPolicy("stadium-events"),
    playStoreUrl = LilloLinks.playStore(BuildConfig.APPLICATION_ID),
    onDismiss = onDismiss
)
