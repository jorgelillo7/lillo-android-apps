package com.jorgelillo.grouppolls

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jorgelillo.grouppolls.data.FakePollRepository
import com.jorgelillo.grouppolls.data.FirestorePollRepository
import com.jorgelillo.grouppolls.data.PollRepository
import com.jorgelillo.grouppolls.data.localStore

/** App-wide singletons (manual DI: the app is small enough not to need Hilt). */
class GroupPollsApplication : Application() {
    val store by lazy { localStore(this) }

    /** Firestore when `firebase.properties` was present at build time; in-memory otherwise. */
    val repository: PollRepository by lazy {
        val app = firebase() ?: return@lazy FakePollRepository()
        FirestorePollRepository(FirebaseFirestore.getInstance(app), FirebaseAuth.getInstance(app))
    }

    /**
     * Initialised by hand instead of with the google-services Gradle plugin: the values come from
     * BuildConfig. App Check proves requests come from this app on a real device (Play Integrity);
     * debug builds use the debug provider, whose token is registered in the Firebase console.
     */
    private fun firebase(): FirebaseApp? {
        if (BuildConfig.FIREBASE_PROJECT_ID.isBlank()) return null
        val options = FirebaseOptions.Builder()
            .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
            .setApplicationId(BuildConfig.FIREBASE_APP_ID)
            .setApiKey(BuildConfig.FIREBASE_API_KEY)
            .build()
        val app = FirebaseApp.initializeApp(this, options)
        FirebaseAppCheck.getInstance(app).installAppCheckProviderFactory(AppCheckProviders.factory())
        return app
    }
}
