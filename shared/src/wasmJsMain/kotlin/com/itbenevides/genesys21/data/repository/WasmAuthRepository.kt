@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.AuthRepository
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlin.js.Promise

// Acessamos via globalThis para contornar restrições do SES/Lockdown
@JsFun("() => !!(globalThis.FirebaseAuthBridge && globalThis.FirebaseAuthBridge.ready)")
private external fun isFirebaseReady(): Boolean

@JsFun("(email, pass) => globalThis.FirebaseAuthBridge.signIn(email, pass)")
private external fun jsSignIn(email: String, pass: String): Promise<JsString>

@JsFun("() => globalThis.FirebaseAuthBridge.signOut()")
private external fun jsSignOut(): Promise<JsAny?>

@JsFun("() => globalThis.FirebaseAuthBridge.getCurrentUserToken()")
private external fun jsGetCurrentUserToken(): Promise<JsString?>

class WasmAuthRepository : AuthRepository {

    private suspend fun ensureReady() {
        var attempts = 0
        while (!isFirebaseReady() && attempts < 100) { 
            delay(100)
            attempts++
        }
    }

    override suspend fun signIn(email: String, password: String): Result<String?> {
        return try {
            ensureReady()
            val token = jsSignIn(email, password).await<JsString>()
            Result.success(token.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            ensureReady()
            jsSignOut().await<JsAny?>()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserToken(): String? {
        return try {
            ensureReady()
            if (!isFirebaseReady()) return null
            jsGetCurrentUserToken().await<JsString?>()?.toString()
        } catch (e: Exception) {
            null
        }
    }
}
