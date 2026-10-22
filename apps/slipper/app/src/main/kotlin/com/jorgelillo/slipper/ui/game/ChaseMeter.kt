package com.jorgelillo.slipper.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jorgelillo.slipper.domain.GameConfig
import com.jorgelillo.slipper.ui.theme.Kitchen

/** Below this gap the meter turns red: she is about to catch you. */
private const val DANGER_GAP = 10.0

/**
 * How close the grandma is, like Temple Run's chaser: the kid on the right, the grandma sliding in
 * from the left as the gap closes. [description] is what a screen reader says.
 */
@Composable
internal fun ChaseMeter(gap: Double, sprites: GameSprites, description: String, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(34.dp).testTag("chase").semantics { contentDescription = description }) {
        val track = size.height * 0.35f
        val top = (size.height - track) / 2
        val close = gap < DANGER_GAP
        drawRoundRect(
            if (close) Kitchen.Shout.copy(alpha = 0.35f) else Kitchen.Grout,
            Offset(0f, top),
            Size(size.width, track),
            CornerRadius(track / 2)
        )
        val kid = sprites["kid_run_1"]
        val kPixel = size.height / kid.height
        kid.draw(this, size.width - kid.width * kPixel, size.height, kPixel)
        val grandma = sprites["grandma_run_1"]
        val gPixel = size.height / grandma.height
        val share = (1 - (gap - GameConfig.CATCH_GAP) / (GameConfig.MAX_GAP - GameConfig.CATCH_GAP)).coerceIn(
            0.0,
            1.0
        ).toFloat()
        val room = size.width - kid.width * kPixel - grandma.width * gPixel
        grandma.draw(this, room * share, size.height, gPixel)
    }
}
