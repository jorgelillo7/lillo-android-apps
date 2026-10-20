package com.jorgelillo.core.platform

/** Public pages every app links to. Privacy policies live in the jorgelillo7.github.io repo. */
object LilloLinks {
    private const val SITE = "https://jorgelillo7.github.io"

    /** `privacy/<slug>/` on the website; the slug is the app's folder name (e.g. "decision-wheel"). */
    fun privacyPolicy(slug: String) = "$SITE/privacy/$slug/"

    fun playStore(packageName: String) = "https://play.google.com/store/apps/details?id=$packageName"
}
