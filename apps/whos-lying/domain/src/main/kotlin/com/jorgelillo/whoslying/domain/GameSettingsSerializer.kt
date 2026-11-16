package com.jorgelillo.whoslying.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * The stored shape of [GameSettings], which also reads what earlier versions wrote: modes
 * CLASSIC (now Mysterious), BLIND (Original without hint), DRIFTER (Mysterious with one
 * journalist), and the `chaos` switch (now the Chaos mode).
 */
@Serializable
private class StoredSettings(
    val mode: String = GameMode.ORIGINAL.name,
    val impostors: Int = 1,
    val impostorHint: Boolean? = null,
    val journalists: Int = 0,
    val chaos: Boolean = false,
    val packIds: Set<String> = emptySet(),
    val discussionSeconds: Int = 0,
    val drawing: Boolean = false,
    val rounds: Int = Match.DEFAULT_ROUNDS
) {
    fun toSettings(): GameSettings {
        val mode = when {
            chaos -> GameMode.CHAOS
            mode == "CLASSIC" || mode == "DRIFTER" -> GameMode.MYSTERIOUS
            mode == "BLIND" -> GameMode.ORIGINAL
            else -> GameMode.entries.firstOrNull { it.name == mode } ?: GameMode.ORIGINAL
        }
        return GameSettings(
            mode = mode,
            impostors = impostors,
            impostorHint = impostorHint ?: (this.mode != "BLIND"),
            journalists = if (this.mode == "DRIFTER") 1 else journalists,
            packIds = packIds,
            discussionSeconds = discussionSeconds,
            drawing = drawing,
            rounds = rounds
        )
    }

    companion object {
        fun of(s: GameSettings) = StoredSettings(
            s.mode.name,
            s.impostors,
            s.impostorHint,
            s.journalists,
            false,
            s.packIds,
            s.discussionSeconds,
            s.drawing,
            s.rounds
        )
    }
}

internal object GameSettingsSerializer : KSerializer<GameSettings> {
    private val stored = StoredSettings.serializer()
    override val descriptor: SerialDescriptor = SerialDescriptor("GameSettings", stored.descriptor)

    override fun serialize(encoder: Encoder, value: GameSettings) =
        encoder.encodeSerializableValue(stored, StoredSettings.of(value))

    override fun deserialize(decoder: Decoder): GameSettings = decoder.decodeSerializableValue(stored).toSettings()
}
