package com.jorgelillo.tournaments.data

import android.content.Context
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.tournaments.domain.Library

/** Every tournament as one JSON document: no database, nothing leaves the phone. */
typealias LibraryRepository = JsonStore<Library>

fun libraryRepository(context: Context): LibraryRepository = JsonStore(context, "tournaments", Library.serializer()) {
    Library()
}
