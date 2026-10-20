package com.jorgelillo.christmascountdown.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jorgelillo.christmascountdown.ChristmasCountdownApplication
import com.jorgelillo.christmascountdown.MainActivity
import com.jorgelillo.christmascountdown.R
import com.jorgelillo.christmascountdown.domain.ChristmasCountdown
import com.jorgelillo.christmascountdown.domain.CountdownState
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.first

/** Home-screen widget with the days (or sleeps) left. Refreshed every 30 minutes and on app changes. */
class CountdownWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as ChristmasCountdownApplication
        val sleepsMode = app.settings.sleepsMode.first()
        val (value, label) = when (val state = ChristmasCountdown.stateAt(ZonedDateTime.now())) {
            CountdownState.ChristmasDay -> "🎄" to context.getString(R.string.widget_christmas)

            is CountdownState.Counting -> {
                val count = if (sleepsMode) state.sleeps else state.timeLeft.days
                val plural = if (sleepsMode) R.plurals.sleeps_left else R.plurals.widget_days
                count.toString() to context.resources.getQuantityString(plural, count.toInt())
            }
        }
        provideContent { WidgetContent(value, label) }
    }
}

@Composable
private fun WidgetContent(value: String, label: String) {
    val white = ColorProvider(Color.White)
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background))
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🎄", style = TextStyle(fontSize = 18.sp))
        Text(text = value, style = TextStyle(color = white, fontSize = 40.sp, fontWeight = FontWeight.Bold))
        Text(
            text = label,
            style = TextStyle(color = ColorProvider(Color(0xFFFFC857)), fontSize = 13.sp, textAlign = TextAlign.Center)
        )
    }
}

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}

suspend fun updateCountdownWidgets(context: Context) {
    CountdownWidget().updateAll(context)
}
