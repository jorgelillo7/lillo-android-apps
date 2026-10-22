package com.jorgelillo.slipper

import android.app.Application
import com.jorgelillo.slipper.data.savedStore

/** App-wide singletons (manual DI: the app is small enough not to need Hilt). */
class SlipperApplication : Application() {
    val store by lazy { savedStore(this) }
}
