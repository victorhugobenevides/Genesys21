package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.AuthRepository
import kotlin.js.Promise

// Interop com as funções globais definidas no index.html
@JsFun("(email, pass) => window.firebaseSignIn(email, pass)")
external fun firebaseSignIn(email: String, pass: String): Promise<JsString>

@JsFun("() => window.firebaseGetToken()")
external fun firebaseGetToken(): Promise<JsString?>

@JsFun("() => window.firebaseSignOut()")
external fun firebaseSignOut(): Promise<JsAny?>

class WasmAuthRepository : AuthRepository {
    
    override suspend fun signIn(email: String, password: String): Result<String?> {
        return try {
            val token = firebaseSignIn(email, password).await().toString()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(Exception(e.message))
        }
    }

    override suspend fun getCurrentUserToken(): String? {
        return try {
            firebaseGetToken().await()?.toString()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun signOut() {
        try {
            firebaseSignOut().await()
        } catch (e: Exception) { }
    }
}

// Extensão para aguardar Promises em Wasm
private suspend fun <T : JsAny?> Promise<T>.await(): T =
    suspendInternal { continuation ->
        this.then(
            { value -> continuation.resumeWith(Result.success(value)); null },
            { error -> continuation.resumeWith(Result.failure(Exception("JS Error"))); null }
        )
    }

private suspend fun <T> suspendInternal(block: (kotlin.coroutines.Continuation<T>) -> Unit): T =
    kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { continuation ->
        block(continuation.intercepted())
        kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
    }

private fun <T> kotlin.coroutines.Continuation<T>.intercepted(): kotlin.coroutines.Continuation<T> = this
