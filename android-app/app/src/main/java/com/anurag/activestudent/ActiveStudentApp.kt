package com.anurag.activestudent

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class ActiveStudentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyBof-k1ReEk9ZK-4wJtCRGKOaL7BLL3Kj4")
                .setApplicationId("1:601731908205:android:0c99287a40fae900977564")
                .setProjectId("active-student-app")
                .setStorageBucket("active-student-app.firebasestorage.app")
                .setGcmSenderId("601731908205")
                .build()
            FirebaseApp.initializeApp(this, options)
        }

        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                )
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (_: Exception) {
            // Ignore if Firestore was already accessed
        }
    }
}

