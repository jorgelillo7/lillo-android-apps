package com.jorgelillo.stadiumevents.widget

import android.content.Context
import androidx.compose.runtime.Composable
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
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jorgelillo.stadiumevents.MainActivity
import com.jorgelillo.stadiumevents.R
import com.jorgelillo.stadiumevents.StadiumEventsApplication
import com.jorgelillo.stadiumevents.domain.Jaleo
import com.jorgelillo.stadiumevents.domain.Level
import com.jorgelillo.stadiumevents.domain.Tomorrow
import com.jorgelillo.stadiumevents.ui.JaleoTexts
import com.jorgelillo.stadiumevents.ui.theme.Ink
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/**
 * Home-screen widget with tomorrow's answer, for people who never open the app. Recomputed every
 * 30 minutes from the copy on the phone (so it moves on at midnight) and after every download.
 */
class JaleoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as StadiumEventsApplication
        val state = app.repository.state.first()
        val texts = JaleoTexts(context.resources)
        val answer = Jaleo.tomorrow(state.feed.events, LocalDate.now())
        val content = when {
            state.fetchedAt == null -> WidgetText(
                context.getString(R.string.widget_name),
                context.getString(R.string.widget_empty),
                "",
                R.drawable.widget_calm
            )

            answer is Tomorrow.Calm -> WidgetText(
                context.getString(R.string.widget_tomorrow),
                context.getString(R.string.widget_calm),
                context.getString(R.string.widget_calm_line),
                R.drawable.widget_calm
            )

            answer is Tomorrow.Busy -> WidgetText(
                context.getString(R.string.widget_tomorrow),
                if (answer.maybe) {
                    context.getString(R.string.widget_maybe, texts.kind(answer.event).lowercase())
                } else {
                    texts.what(answer.event)
                },
                texts.title(answer.event),
                if (answer.event.level == Level.MAX) R.drawable.widget_max else R.drawable.widget_busy
            )

            else -> error("unreachable")
        }
        provideContent { WidgetContent(content) }
    }
}

private data class WidgetText(val label: String, val headline: String, val line: String, val background: Int)

@Composable
private fun WidgetContent(text: WidgetText) {
    val ink = ColorProvider(Ink)
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(text.background))
            .clickable(actionStartActivity<MainActivity>())
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text.label, style = TextStyle(color = ink, fontSize = 16.sp, fontWeight = FontWeight.Bold))
        Text(
            text.headline,
            style = TextStyle(color = ink, fontSize = 24.sp, fontWeight = FontWeight.Bold),
            maxLines = 2
        )
        if (text.line.isNotEmpty()) {
            Text(text.line, style = TextStyle(color = ink, fontSize = 15.sp), maxLines = 2)
        }
    }
}

class JaleoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = JaleoWidget()
}

suspend fun updateJaleoWidgets(context: Context) {
    JaleoWidget().updateAll(context)
}
