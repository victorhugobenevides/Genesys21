package com.itbenevides.genesys21.ui.components.organisms.payment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.util.GenesysUUID
import com.itbenevides.genesys21.util.StripeBridge
import kotlinx.coroutines.launch

@Composable
actual fun StripePaymentElement(
    modifier: Modifier,
    clientSecret: String,
    publishableKey: String,
    appearanceJson: String,
    onPaymentConfirmed: () -> Unit,
    onPaymentError: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val elementId = remember { "stripe-payment-element-${GenesysUUID.randomUUID()}" }
    var isMounted by remember { mutableStateOf(false) }

    LaunchedEffect(clientSecret) {
        StripeBridge.initialize(publishableKey)
        StripeBridge.mountPaymentElement(clientSecret, appearanceJson, elementId)
            .onSuccess { isMounted = true }
            .onFailure { onPaymentError(it.message ?: "Erro ao carregar Stripe") }
    }

    DisposableEffect(elementId) {
        onDispose {
            scope.launch {
                StripeBridge.unmountPaymentElement(elementId)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Pagamento Seguro via Stripe", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))

        if (!isMounted) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Spacer(Modifier.height(300.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    val returnUrl = com.itbenevides.genesys21.getWebBaseUrl() + "/track/success"
                    StripeBridge.confirmPayment(returnUrl).onFailure {
                        onPaymentError(it.message ?: "Erro ao confirmar")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Pagar Agora")
        }
    }
}
