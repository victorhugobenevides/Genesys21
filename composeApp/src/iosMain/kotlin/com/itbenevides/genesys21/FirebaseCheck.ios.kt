package com.itbenevides.genesys21

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

actual fun isFirebaseAvailable(): Boolean {
    return try {
        Firebase.auth
        true
    } catch (e: Exception) {
        false
    }
}
