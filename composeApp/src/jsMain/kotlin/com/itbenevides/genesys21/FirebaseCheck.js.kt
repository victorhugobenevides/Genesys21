package com.itbenevides.genesys21

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.initialize

private var isInitialized = false

actual fun isFirebaseAvailable(): Boolean {
    if (!isInitialized) {
        try {
            Firebase.initialize(
                options = FirebaseOptions(
                    apiKey = "AIzaSyCq22tklAK0iQd4jWDINkJZAS9-I_-dLSY",
                    authDomain = "genesys21-32035.firebaseapp.com",
                    applicationId = "1:674755208954:web:26e7b20a54f9ceb0dc4b43",
                    projectId = "genesys21-32035",
                    storageBucket = "genesys21-32035.firebasestorage.app"
                )
            )
            isInitialized = true
        } catch (e: Exception) {
            return false
        }
    }
    
    return try {
        Firebase.auth
        true
    } catch (e: Exception) {
        false
    }
}
