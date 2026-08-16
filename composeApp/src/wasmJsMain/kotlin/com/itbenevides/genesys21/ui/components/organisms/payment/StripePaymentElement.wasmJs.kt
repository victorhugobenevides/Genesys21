package com.itbenevides.genesys21.ui.components.organisms.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.util.StripeBridge
import com.itbenevides.genesys21.util.GenesysUUID
import kotlinx.coroutines.launch

@Composable
actual fun StripePaymentElement(
    modifier: Modifier,
    clientSecret: String,
    publishableKey: String,
    appearanceJson: String,
    onPaymentConfirmed: () -> Unit,
    onPaymentError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val elementId = remember { "stripe-payment-element-${GenesysUUID.randomUUID()}" }

    LaunchedEffect(clientSecret) {
        StripeBridge.initialize(publishableKey)
        StripeBridge.mountPaymentElement(clientSecret, appearanceJson, elementId)
            .onFailure { onPaymentError(it.message ?: "Erro ao carregar Stripe") }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Pagamento Seguro via Stripe", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))

        // No WasmJs, o Stripe vai tentar montar neste ID usando document.getElementById
        Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
            // Em uma implementação real de Wasm, o bridge cuidaria de injetar um div HTML
            // aqui ou redirecionar o render para fora do canvas.
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pagar Agora")
        }
    }
}
