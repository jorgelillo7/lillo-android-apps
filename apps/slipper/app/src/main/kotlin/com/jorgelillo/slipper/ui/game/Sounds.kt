package com.jorgelillo.slipper.ui.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.jorgelillo.slipper.R
import com.jorgelillo.slipper.domain.EventKind

/** The game's short sounds (res/raw, made by tools/generate_sounds.py), one per kind of event. */
internal class Sounds(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids: Map<EventKind, Int> = mapOf(
        EventKind.JUMP to R.raw.sfx_jump,
        EventKind.CROQUETA to R.raw.sfx_croqueta,
        EventKind.NEAR_MISS to R.raw.sfx_near_miss,
        EventKind.HIT to R.raw.sfx_hit,
        EventKind.STUMBLE to R.raw.sfx_hit,
        EventKind.BLOCKED to R.raw.sfx_blocked,
        EventKind.POWER to R.raw.sfx_power,
        EventKind.CRASH to R.raw.sfx_crash,
        EventKind.CAUGHT to R.raw.sfx_caught
    ).mapValues { (_, res) -> pool.load(context, res, 1) }

    /** Croquetas in a combo sound a little higher each time. */
    fun play(kind: EventKind, combo: Int = 1) {
        val id = ids[kind] ?: return
        val rate = if (kind == EventKind.CROQUETA) (1f + 0.08f * (combo - 1)).coerceAtMost(1.5f) else 1f
        pool.play(id, 0.8f, 0.8f, 1, 0, rate)
    }

    fun release() = pool.release()
}

@Composable
internal fun rememberSounds(): Sounds {
    val context = LocalContext.current
    val sounds = remember { Sounds(context.applicationContext) }
    DisposableEffect(sounds) { onDispose { sounds.release() } }
    return sounds
}
