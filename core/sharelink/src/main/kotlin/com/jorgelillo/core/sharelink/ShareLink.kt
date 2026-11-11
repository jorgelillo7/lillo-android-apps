package com.jorgelillo.core.sharelink

import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.util.Base64
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Something (a tournament, a word pack…) inside a link, so it moves to another phone with no
 * account or server: compact JSON, raw deflate, base64url, prefixed with the format version, in
 * the fragment of `https://jorgelillo7.github.io<path>#<code>` (the website never receives it).
 * The site's page at <path> can decode and show it, and its "open in the app" button passes the
 * code as `?d=` because Android intent links use the `#` themselves.
 */
class ShareLink<T>(private val serializer: KSerializer<T>, val path: String) {

    init {
        require(path.startsWith("/") && path.endsWith("/")) { "path like /t/" }
    }

    fun encode(value: T): String {
        val bytes = json.encodeToString(serializer, value).toByteArray()
        val deflater = Deflater(Deflater.BEST_COMPRESSION, true).apply {
            setInput(bytes)
            finish()
        }
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        while (!deflater.finished()) out.write(buffer, 0, deflater.deflate(buffer))
        deflater.end()
        return VERSION + Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray())
    }

    /**
     * The value in [code], or null if it isn't one of ours (truncated, other version, other type…)
     * or is larger than any link the apps make: a code over [MAX_CODE_CHARS], or one that inflates
     * past [MAX_JSON_BYTES], is refused before it is parsed.
     */
    fun decode(code: String): T? {
        if (!code.startsWith(VERSION) || code.length > MAX_CODE_CHARS) return null
        return runCatching {
            val bytes = Base64.getUrlDecoder().decode(code.substring(VERSION.length))
            val inflater = Inflater(true).apply { setInput(bytes) }
            try {
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                while (!inflater.finished()) {
                    val n = inflater.inflate(buffer)
                    if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) error("truncated")
                    if (out.size() + n > MAX_JSON_BYTES) error("too large")
                    out.write(buffer, 0, n)
                }
                json.decodeFromString(serializer, out.toString(Charsets.UTF_8.name()))
            } finally {
                inflater.end()
            }
        }.getOrNull()
    }

    fun link(value: T): String = "https://$HOST$path#${encode(value)}"

    /** The code in a shared link, in the website's `?d=` link, inside a chat message, or bare. */
    fun codeFromLink(text: String): String? {
        val link = text.split(Regex("\\s+")).firstOrNull { "$HOST$path" in it } ?: text.trim()
        val rest = link.substringAfter("$HOST$path", "")
        return rest.substringAfter('#', "").takeIf { it.isNotBlank() }
            ?: rest.substringAfter("d=", "").substringBefore('&').takeIf { it.isNotBlank() }
                ?.let { URLDecoder.decode(it, "UTF-8") }
            ?: link.takeIf { it.startsWith(VERSION) && ' ' !in it }
    }

    /** Decodes whatever [text] holds: a link, a chat message with one, or a bare code. */
    fun fromText(text: String): T? = codeFromLink(text)?.let(::decode)

    companion object {
        const val HOST = "jorgelillo7.github.io"
        const val VERSION = "1"

        /** The longest code accepted: far above any real tournament or pack, far below a memory problem. */
        const val MAX_CODE_CHARS = 32_000

        /** The most JSON a code may inflate to; a link that expands past it is refused. */
        const val MAX_JSON_BYTES = 256 * 1024

        /** Longer links are still shared as text, but no QR code: too dense to scan off a screen. */
        const val QR_MAX_CHARS = 1_800

        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }
    }
}
