package com.jorgelillo.core.platform

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedTextTest {
    @Test
    fun anOpenedLink() {
        val link = "https://jorgelillo7.github.io/t/#abc"
        assertEquals(link, Intent(Intent.ACTION_VIEW, Uri.parse(link)).sharedText())
    }

    @Test
    fun textSharedToTheApp() {
        val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "a pack")
        assertEquals("a pack", intent.sharedText())
    }

    @Test
    fun aPlainLaunchHasNothing() {
        assertNull(Intent(Intent.ACTION_MAIN).sharedText())
        assertNull((null as Intent?).sharedText())
    }
}
