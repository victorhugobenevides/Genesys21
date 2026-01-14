package com.itbenevides.genesys21

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.apps

class JsAuthRepository : AuthRepository {
    
    init {
        println("JsAuthRepository: Inicializando...")
        ensureFirebaseInitialized()
    }

    private fun ensureFirebaseInitialized() {
        try {
            // Corrigido: apps é uma função no JS
            if (Firebase.apps().isEmpty()) {
                println("JsAuthRepository: Nenhum app encontrado. Inicializando Firebase...")
                Firebase.initialize(
                    options = FirebaseOptions(
                        apiKey = "AIzaSyCq22tklAK0iQd4jWDINkJZAS9-I_-dLSY",
                        authDomain = "genesys21-32035.firebaseapp.com",
                        applicationId = "1:674755208954:web:26e7b20a54f9ceb0dc4b43",
                        projectId = "genesys21-32035",
                        storageBucket = "genesys21-32035.firebasestorage.app"
                    )
                )
                println("JsAuthRepository: Firebase inicializado com sucesso.")
            } else {
                println("JsAuthRepository: Firebase já estava inicializado.")
            }
        } catch (e: Exception) {
            println("JsAuthRepository: Erro crítico na inicialização: ${e.message}")
        }
    }

    private val auth get() = Firebase.auth

    override suspend fun signIn(email: String, password: String): Result<String?> {
        println("JsAuthRepository: Iniciando signIn para $email")
        ensureFirebaseInitialized() 
        return try {
            auth.signInWithEmailAndPassword(email, password)
            val token = getCurrentUserToken()
            println("JsAuthRepository: signIn sucesso.")
            Result.success(token)
        } catch (e: Exception) {
            println("JsAuthRepository: signIn falha: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserToken(): String? {
        ensureFirebaseInitialized()
        return try {
            val token = auth.currentUser?.getIdToken(false)
            println("JsAuthRepository: Token recuperado.")
            token
        } catch (e: Exception) {
            println("JsAuthRepository: Erro ao recuperar token: ${e.message}")
            null
        }
    }

    override suspend fun signOut() {
        println("JsAuthRepository: Executando signOut")
        ensureFirebaseInitialized()
        try {
            auth.signOut()
        } catch (e: Exception) {
            println("JsAuthRepository: Erro no signOut: ${e.message}")
        }
    }
}

actual fun getAuthRepository(): AuthRepository = JsAuthRepository()
