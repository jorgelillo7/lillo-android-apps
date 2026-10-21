package com.jorgelillo.decisionwheel.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jorgelillo.decisionwheel.DecisionWheelApplication
import com.jorgelillo.decisionwheel.data.Presets
import com.jorgelillo.decisionwheel.data.Shortcuts
import com.jorgelillo.decisionwheel.domain.Wheel
import com.jorgelillo.decisionwheel.ui.about.AboutSheet
import com.jorgelillo.decisionwheel.ui.edit.EditScreen
import com.jorgelillo.decisionwheel.ui.home.HomeScreen
import com.jorgelillo.decisionwheel.ui.wheel.WheelScreen
import kotlinx.coroutines.delay

private const val NEW = "new"
private const val MISSING_WHEEL_GRACE_MILLIS = 600L

@Composable
fun DecisionWheelApp(app: DecisionWheelApplication, openWheelId: String? = null) {
    val viewModel: WheelsViewModel = viewModel(factory = WheelsViewModel.factory(app.repository))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val current = state ?: return // first disk read in progress (a few ms)
    val nav = rememberNavController()
    var showAbout by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    // Launcher shortcuts follow favourites and recent use.
    val shortcuts = current.shortcutWheels()
    LaunchedEffect(shortcuts) { Shortcuts.update(context, shortcuts) }
    // Opened from a shortcut: straight to that wheel (once, not after rotation).
    var openedShortcut by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(openWheelId) {
        if (openWheelId != null && !openedShortcut && current.wheel(openWheelId) != null) {
            openedShortcut = true
            nav.navigate("wheel/$openWheelId")
        }
    }

    NavHost(nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                state = current,
                onOpen = { nav.navigate("wheel/${it.id}") },
                onNew = { nav.navigate("edit/$NEW") },
                onAvoidRepeats = viewModel::setAvoidRepeats,
                onSound = viewModel::setSound,
                onAbout = { showAbout = true },
                onToggleFavorite = { viewModel.toggleFavorite(it.id) },
                onRestorePresets = { viewModel.restorePresets(Presets.seed(context)) }
            )
        }
        composable("wheel/{id}") { entry ->
            val wheel = current.wheel(entry.arguments?.getString("id").orEmpty())
            if (wheel == null) {
                // Either just created (the save is still landing) or deleted: wait briefly, then leave.
                LaunchedEffect(Unit) {
                    delay(MISSING_WHEEL_GRACE_MILLIS)
                    nav.popBackStack()
                }
                return@composable
            }
            LaunchedEffect(wheel.id) { Shortcuts.reportUsed(context, wheel.id) }
            WheelScreen(
                wheel = wheel,
                state = current,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate("edit/${wheel.id}") },
                onAccept = { viewModel.accept(wheel.id, it) },
                onClearHistory = { viewModel.clearHistory(wheel.id) },
                onTick = { app.tickPlayer.tick() },
                onToggleFavorite = { viewModel.toggleFavorite(wheel.id) }
            )
        }
        composable("edit/{id}") { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            val isNew = id == NEW
            val initial = remember(id) { if (isNew) Wheel(viewModel.newWheelId(), "", emptyList()) else current.wheel(id) }
            if (initial == null) {
                LaunchedEffect(Unit) { nav.popBackStack() }
                return@composable
            }
            EditScreen(
                initial = initial,
                isNew = isNew,
                onSave = { wheel ->
                    viewModel.save(wheel)
                    nav.popBackStack()
                    if (isNew) nav.navigate("wheel/${wheel.id}")
                },
                onDelete = {
                    viewModel.delete(initial.id)
                    nav.popBackStack("home", inclusive = false)
                },
                onBack = { nav.popBackStack() }
            )
        }
    }

    if (showAbout) AboutSheet(onDismiss = { showAbout = false })
}
