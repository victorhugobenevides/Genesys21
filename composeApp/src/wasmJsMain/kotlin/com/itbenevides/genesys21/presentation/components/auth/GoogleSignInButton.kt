package com.itbenevides.genesys21.presentation.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import kotlinx.coroutines.launch
import kotlin.js.Promise
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { if (typeof window.firebaseSignInGoogle === 'function') { return window.firebaseSignInGoogle(); } else { return Promise.reject('Firebase JS functions not found in window object'); } }")
external fun firebaseSignInGoogleSafe(): Promise<JsString>

// Extensão para aguardar Promises em Wasm
private suspend fun <T : JsAny?> Promise<T>.await(): T =
    suspendInternal { continuation ->
        this.then(
            { value ->
                continuation.resumeWith(Result.success(value))
                null
            },
            { error ->
                continuation.resumeWith(Result.failure(Exception("JS Error: " + error.toString())))
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

@Composable
actual fun GoogleSignInButton(
    modifier: Modifier,
    onTokenReceived: (idToken: String, accessToken: String?) -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    GenesysLoadingButton(
        text = "Entrar com Google",
        onClick = {
            scope.launch {
                try {
                    val token = firebaseSignInGoogleSafe().await().toString()
                    onTokenReceived(token, null)
                } catch (e: Exception) {
                    println("WASM Login Error: \${e.message}")
                    onError(e.message ?: "Erro ao entrar com Google")
                }
            }
        },
        modifier = modifier
    )
}
