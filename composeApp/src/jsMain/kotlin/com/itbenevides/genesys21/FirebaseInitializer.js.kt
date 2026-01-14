package com.itbenevides.genesys21

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

actual fun initializeFirebase() {
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
    } catch (e: Exception) {
        println("JS: Firebase já inicializado ou erro: ${e.message}")
    }
}
