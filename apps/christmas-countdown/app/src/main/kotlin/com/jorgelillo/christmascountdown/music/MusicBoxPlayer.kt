package com.jorgelillo.christmascountdown.music

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.jorgelillo.christmascountdown.domain.Carols
import com.jorgelillo.christmascountdown.domain.Melody
import com.jorgelillo.christmascountdown.domain.MusicBoxSynth
import com.jorgelillo.christmascountdown.domain.ShuffledPlaylist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Plays the carols one after another in shuffled order, forever. Each carol is synthesised on
 * the device the first time it comes up (no audio files in the APK) and streamed in small chunks.
 */
class MusicBoxPlayer(private val scope: CoroutineScope) {

    private val sampleRate = MusicBoxSynth.DEFAULT_SAMPLE_RATE
    private val chunkSize = sampleRate / 10
    private val playlist = ShuffledPlaylist(Carols.all)
    private val rendered = mutableMapOf<Melody, ShortArray>()
    private val gap = ShortArray(sampleRate) // one second of silence between carols
    private var job: Job? = null

    fun play() {
        if (job?.isActive == true) return
        job = scope.launch(Dispatchers.Default) {
            val track = createTrack()
            try {
                track.play()
                while (isActive) {
                    val carol = playlist.next()
                    val pcm = rendered.getOrPut(carol) { MusicBoxSynth.render(carol, sampleRate, loop = false) }
                    stream(track, pcm)
                    stream(track, gap)
                }
            } finally {
                track.pause()
                track.flush()
                track.release()
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    /** Writes [pcm] in small chunks so stop() takes effect within ~100 ms. */
    private fun CoroutineScope.stream(track: AudioTrack, pcm: ShortArray) {
        var position = 0
        while (isActive && position < pcm.size) {
            val count = minOf(chunkSize, pcm.size - position)
            track.write(pcm, position, count)
            position += count
        }
    }

    private fun createTrack(): AudioTrack {
        val minBuffer = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(maxOf(minBuffer, chunkSize * 2 * 2))
            .build()
            .apply { setVolume(0.6f) }
    }
}
