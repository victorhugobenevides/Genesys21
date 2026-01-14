package com.itbenevides.genesys21

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

actual fun isFirebaseAvailable(): Boolean {
    // Agora que inicializamos no main.kt, apenas verificamos se conseguimos acessar o Auth
    return try {
        Firebase.auth
        true
    } catch (e: Exception) {
        false
    }
}
