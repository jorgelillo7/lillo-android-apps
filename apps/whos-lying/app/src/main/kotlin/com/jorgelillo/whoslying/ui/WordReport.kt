package com.jorgelillo.whoslying.ui

import android.net.Uri
import androidx.core.net.toUri

/**
 * "Report this word" goes to a Google Form, opened prefilled in the browser: the player only taps
 * Send, and the app itself never touches the network. The form is created by
 * tools/create_report_form.gs; these are its field ids.
 */
object WordReport {
    private const val FORM_URL =
        "https://docs.google.com/forms/d/e/1FAIpQLSf36XFjNcuWZLrEj4Msc01SdGrY8a-2GlxUy_DqQ-quov1dAQ/viewform"
    private const val WORD = "entry.753030629"
    private const val DECOY = "entry.1384133815"
    private const val PACK = "entry.1887407200"
    private const val LANGUAGE = "entry.755588422"
    private const val REASON = "entry.1131699029"
    private const val COMMENT = "entry.1013901262"

    fun url(word: String, decoy: String?, pack: String, language: String, reasons: List<String>, comment: String): Uri =
        FORM_URL.toUri().buildUpon()
            .appendQueryParameter("usp", "pp_url")
            .appendQueryParameter(WORD, word)
            .appendQueryParameter(DECOY, decoy.orEmpty())
            .appendQueryParameter(PACK, pack)
            .appendQueryParameter(LANGUAGE, language)
            .appendQueryParameter(REASON, reasons.joinToString(", "))
            .appendQueryParameter(COMMENT, comment.trim())
            .build()
}
