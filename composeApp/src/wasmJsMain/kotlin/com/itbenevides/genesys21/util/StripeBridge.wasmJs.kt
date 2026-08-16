package com.itbenevides.genesys21.util

import kotlin.js.Promise

@JsFun("(key) => { if (typeof window.stripeInitialize === 'function') window.stripeInitialize(key); }")
external fun stripeInitializeSafe(publishableKey: String)

@JsFun("(secret, appearance, id) => { if (typeof window.stripeMountPaymentElement === 'function') return window.stripeMountPaymentElement(secret, appearance, id); return Promise.reject('JS Not Ready'); }")
external fun stripeMountPaymentElementSafe(clientSecret: String, appearanceJson: String, elementId: String): Promise<JsAny?>

@JsFun("(url) => { if (typeof window.stripeConfirmPayment === 'function') return window.stripeConfirmPayment(url); return Promise.reject('JS Not Ready'); }")
external fun stripeConfirmPaymentSafe(returnUrl: String): Promise<JsAny?>

object StripeBridge {
    fun initialize(publishableKey: String) {
        stripeInitializeSafe(publishableKey)
    }

    suspend fun mountPaymentElement(clientSecret: String, appearanceJson: String, elementId: String): Result<Unit> {
        return try {
            stripeMountPaymentElementSafe(clientSecret, appearanceJson, elementId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmPayment(returnUrl: String): Result<Unit> {
        return try {
            stripeConfirmPaymentSafe(returnUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Reuse await from WasmAuthRepository or similar
private suspend fun <T : JsAny?> Promise<T>.await(): T =
    suspendInternal { continuation ->
        this.then(
            { value ->
                continuation.resumeWith(Result.success(value))
                null
            },
            { error ->
                val errorMessage = error.toString()
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
