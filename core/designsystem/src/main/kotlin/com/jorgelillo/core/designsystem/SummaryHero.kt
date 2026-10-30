package com.jorgelillo.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Each chip comes in this much after the one before it. */
private const val CHIP_DELAY_MILLIS = 120

/** One status line in a [LilloSummaryHero]: what to do next, the last champion, a record… */
data class HeroChip(val text: String, val tag: String? = null)

/**
 * A home screen's header: a gradient card with the app's state at a glance (a [title], a one-line
 * [subtitle] such as "2 in progress · 1 finished", and [chips] for what matters now), a big [emoji]
 * and a few translucent circles. On short screens (landscape, handhelds) it shrinks to a strip so
 * the list below keeps the room. It fades in on first show, the chips one after another.
 *
 * [colors] is the gradient, start to end: dark enough for [contentColor] (white by default) to
 * keep 4.5:1, which the screenshot tests check.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LilloSummaryHero(
    title: String,
    subtitle: String,
    colors: List<Color>,
    emoji: String,
    modifier: Modifier = Modifier,
    chips: List<HeroChip> = emptyList(),
    contentColor: Color = Color.White
) {
    val compact = isShortScreen()
    Box(
        modifier
            .entrance()
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (compact) 18.dp else 24.dp))
            .background(Brush.linearGradient(colors))
            .testTag("summary_hero")
    ) {
        Canvas(Modifier.matchParentSize()) {
            val h = size.height
            drawCircle(Color.White.copy(alpha = 0.07f), h * 0.55f, Offset(size.width * 0.82f, h * 0.2f))
            drawCircle(Color.White.copy(alpha = 0.06f), h * 0.35f, Offset(size.width * 0.62f, h * 0.95f))
            drawCircle(Color.White.copy(alpha = 0.05f), h * 0.22f, Offset(size.width * 0.95f, h * 0.85f))
        }
        val chipRow = @Composable {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEachIndexed { i, chip ->
                    HeroChipText(chip, contentColor, compact, Modifier.entrance(CHIP_DELAY_MILLIS * (i + 1)))
                }
            }
        }
        if (compact) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 28.sp, modifier = Modifier.clearAndSetSemantics {})
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "$title · $subtitle",
                        color = contentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (chips.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        chipRow()
                    }
                }
            }
        } else {
            Text(
                emoji,
                fontSize = 56.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).clearAndSetSemantics {}
            )
            Column(Modifier.padding(20.dp)) {
                // Clear of the emoji in the corner; the chips below may run under it.
                val clear = Modifier.padding(end = 72.dp)
                Text(title, color = contentColor, fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = clear)
                Text(
                    subtitle,
                    color = contentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = clear
                )
                if (chips.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    chipRow()
                }
            }
        }
    }
}

@Composable
private fun HeroChipText(chip: HeroChip, color: Color, compact: Boolean, modifier: Modifier = Modifier) {
    Text(
        chip.text,
        color = color,
        fontSize = if (compact) 13.sp else 14.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(Color(0x40000000), RoundedCornerShape(50))
            .padding(horizontal = if (compact) 10.dp else 14.dp, vertical = if (compact) 4.dp else 8.dp)
            .then(if (chip.tag != null) Modifier.testTag(chip.tag) else Modifier)
    )
}
