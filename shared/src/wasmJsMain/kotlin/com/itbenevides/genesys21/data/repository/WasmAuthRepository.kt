package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.js.Promise

// Interop seguro: Se a função não existir no window, retorna um valor padrão em vez de crashar
@JsFun("""(email, pass) => {
    if (typeof window.firebaseSignIn === 'function') {
        return window.firebaseSignIn(email, pass);
    } else {
        console.error('DEBUG: window.firebaseSignIn não encontrado');
        return Promise.reject('JS Not Ready - window.firebaseSignIn missing');
    }
}""")
external fun firebaseSignInSafe(email: String, pass: String): Promise<JsString>

@JsFun("""(email, pass) => {
    if (typeof window.firebaseSignUp === 'function') {
        return window.firebaseSignUp(email, pass);
    } else {
        console.error('DEBUG: window.firebaseSignUp não encontrado');
        return Promise.reject('JS Not Ready - window.firebaseSignUp missing');
    }
}""")
external fun firebaseSignUpSafe(email: String, pass: String): Promise<JsString>

@JsFun("""() => {
    if (typeof window.firebaseSignInGoogle === 'function') {
        return window.firebaseSignInGoogle();
    } else {
        console.error('DEBUG: window.firebaseSignInGoogle não encontrado');
        return Promise.reject('JS Not Ready - window.firebaseSignInGoogle missing');
    }
}""")
external fun firebaseSignInGoogleSafe(): Promise<JsString>

@JsFun("() => (typeof window.firebaseGetToken === 'function') ? window.firebaseGetToken() : Promise.resolve(null)")
external fun firebaseGetTokenSafe(): Promise<JsString?>

@JsFun("() => (typeof window.firebaseGetUserId === 'function') ? window.firebaseGetUserId() : Promise.resolve(null)")
external fun firebaseGetUserIdSafe(): Promise<JsString?>

@JsFun("() => (typeof window.firebaseGetUserEmail === 'function') ? window.firebaseGetUserEmail() : Promise.resolve(null)")
external fun firebaseGetUserEmailSafe(): Promise<JsString?>

@JsFun("() => (typeof window.firebaseGetUserName === 'function') ? window.firebaseGetUserName() : Promise.resolve(null)")
external fun firebaseGetUserNameSafe(): Promise<JsString?>

@JsFun("() => (typeof window.firebaseSignOut === 'function') ? window.firebaseSignOut() : Promise.resolve(null)")
external fun firebaseSignOutSafe(): Promise<JsAny?>

@JsFun("() => { if (typeof window.firebaseInitializeOneTap === 'function') window.firebaseInitializeOneTap(); }")
external fun firebaseInitializeOneTapSafe()

@JsFun("(callback) => { if (typeof window.firebaseOnAuthChanged === 'function') window.firebaseOnAuthChanged(callback); }")
external fun firebaseOnAuthChangedSafe(callback: (JsString?) -> Unit)

@JsFun("(str) => atob(str)")
external fun decodeBase64Safe(str: String): String

class WasmAuthRepository : AuthRepository {
    override val authState: Flow<String?> = callbackFlow {
        firebaseOnAuthChangedSafe { uid ->
            trySend(uid?.toString())
        }
        awaitClose { }
    }

    override suspend fun signIn(email: String, password: String): Result<String?> {
        println("DEBUG KOTLIN: Tentando login para $email")
        val promise = firebaseSignInSafe(email, password)
        println("DEBUG KOTLIN: Promise criada")
        return try {
            val token = promise.await().toString()
            println("DEBUG KOTLIN: Token recebido: $token")
            Result.success(token)
        } catch (e: Exception) {
            println("DEBUG KOTLIN: Erro capturado: $e")
            Result.failure(e)
        }
    }

    override suspend fun signIn(idToken: String, accessToken: String?, provider: String): Result<String?> {
        return try {
            if (provider == "google") {
                if (idToken.isNotBlank()) {
                    // Se o token já foi obtido (ex: via GoogleSignInButton), não dispara o popup de novo
                    println("WASM: Usando token Google existente.")
                    Result.success(idToken)
                } else {
                    println("WASM: Disparando login Google (Token Vazio)...")
                    val token = firebaseSignInGoogleSafe().await().toString()
                    println("WASM: Login Google sucesso! Token obtido.")
                    Result.success(token)
                }
            } else {
                Result.success(idToken)
            }
        } catch (e: Exception) {
            println("WASM: Falha crítica no Login Google: \${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String): Result<String?> {
        return try {
            val token = firebaseSignUpSafe(email, password).await().toString()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserToken(): String? {
        return try {
            firebaseGetTokenSafe().await()?.toString()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return try {
            firebaseGetUserIdSafe().await()?.toString()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCurrentUserEmail(): String? {
        return try {
            val email = firebaseGetUserEmailSafe().await()?.toString()
            if (!email.isNullOrBlank()) return email

            // Fallback: Tenta decodificar o e-mail do ID Token
            getCurrentUserToken()?.let { token ->
                extractEmailFromToken(token)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractEmailFromToken(token: String): String? {
        return try {
            // JWT format is Header.Payload.Signature
            val parts = token.split(".")
            if (parts.size < 2) return null

            val payload = parts[1]
            // Wasm/JS Base64 decode
            val decoded = decodeBase64Safe(payload)
            if (decoded.contains("\"email\":\"")) {
                decoded.substringAfter("\"email\":\"").substringBefore("\"")
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCurrentUserName(): String? {
        return try {
            firebaseGetUserNameSafe().await()?.toString()
        } catch (e: Exception) {
            null
        }
    }

    override fun initializeOneTap() {
        println("WASM: Chamando inicialização do One Tap...")
        firebaseInitializeOneTapSafe()
    }

    override suspend fun signOut() {
        try {
            firebaseSignOutSafe().await()
        } catch (e: Exception) {
        }
    }
}

// Extensões de Promise para Wasm
private suspend fun <T : JsAny?> Promise<T>.await(): T =
    suspendInternal { continuation ->
        this.then(
            { value ->
                continuation.resumeWith(Result.success(value))
                null
            },
            { error ->
                val errorMessage = error?.toString() ?: "JS Error"
                continuation.resumeWith(Result.failure(Exception(errorMessage)))
                null
            },
        )
    }

private suspend fun <T> suspendInternal(block: (kotlin.coroutines.Continuation<T>) -> Unit): T =
    kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { continuation ->
        block(continuation.intercepted())
        kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
    }

private fun <T> kotlin.coroutines.Continuation<T>.intercepted(): kotlin.coroutines.Continuation<T> = this
