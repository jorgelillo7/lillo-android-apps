package com.jorgelillo.decisionwheel.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.jorgelillo.decisionwheel.domain.ClickSynth
import java.io.File

/** Low-latency "tick" played each time the pointer passes a segment. Synthesised once, no assets. */
class TickPlayer(context: Context) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundId: Int = File(context.cacheDir, "tick.wav").let { file ->
        if (!file.exists()) file.writeBytes(ClickSynth.wav(ClickSynth.render()))
        pool.load(file.path, 1)
    }

    fun tick(volume: Float = 0.7f) {
        pool.play(soundId, volume, volume, 1, 0, 1f)
    }
}
