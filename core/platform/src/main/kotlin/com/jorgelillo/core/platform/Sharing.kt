package com.jorgelillo.core.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** Opens the share sheet with plain text (a message, a link). */
fun Context.shareText(text: String, chooserTitle: String? = null) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(send, chooserTitle))
}

/**
 * What another app handed over: an opened link (ACTION_VIEW, e.g. a share link or App Link) or
 * text shared to this app (ACTION_SEND). Null for a plain launch.
 */
fun Intent?.sharedText(): String? = when (this?.action) {
    Intent.ACTION_VIEW -> dataString
    Intent.ACTION_SEND -> getStringExtra(Intent.EXTRA_TEXT)
    else -> null
}

/** A file in the cache folder that [shareFile] can hand to other apps. Overwritten each time. */
fun Context.sharedCacheFile(name: String): File = File(File(cacheDir, "shared").apply { mkdirs() }, name)

/** Opens the share sheet with a file from [sharedCacheFile] (an image, a CSV…) and optional text. */
fun Context.shareFile(file: File, mimeType: String, text: String? = null, subject: String? = null) {
    val uri = FileProvider.getUriForFile(this, "$packageName.files", file)
    val send = Intent(Intent.ACTION_SEND)
        .setType(mimeType)
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    text?.let { send.putExtra(Intent.EXTRA_TEXT, it) }
    subject?.let { send.putExtra(Intent.EXTRA_SUBJECT, it) }
    startActivity(Intent.createChooser(send, null))
}
