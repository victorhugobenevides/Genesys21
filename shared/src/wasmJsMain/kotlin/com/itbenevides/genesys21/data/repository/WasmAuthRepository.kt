package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.AuthRepository
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

@JsFun("() => (typeof window.firebaseSignOut === 'function') ? window.firebaseSignOut() : Promise.resolve(null)")
external fun firebaseSignOutSafe(): Promise<JsAny?>

class WasmAuthRepository : AuthRepository {
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
                println("WASM: Disparando login Google...")
                val token = firebaseSignInGoogleSafe().await().toString()
                println("WASM: Login Google sucesso! Token obtido.")
                Result.success(token)
            } else {
                Result.success(idToken)
            }
        } catch (e: Exception) {
            println("WASM: Falha crítica no Login Google: ${e.message}")
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
