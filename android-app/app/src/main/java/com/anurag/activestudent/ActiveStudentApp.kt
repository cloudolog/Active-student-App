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
                .setApiKey("AIzaSyClARhFkDSzzLC1gmZbG1GNTU1j-6Iqr4g")
                .setApplicationId("1:758871430641:web:9cf49554e093a7e5a2e050")
                .setProjectId("activestudent-cloudolog")
                .setStorageBucket("activestudent-cloudolog.firebasestorage.app")
                .setGcmSenderId("758871430641")
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

