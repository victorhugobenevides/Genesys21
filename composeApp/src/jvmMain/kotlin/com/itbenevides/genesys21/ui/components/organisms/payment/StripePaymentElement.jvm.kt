package com.itbenevides.genesys21.ui.components.organisms.payment

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun StripePaymentElement(
    modifier: Modifier,
    clientSecret: String,
    publishableKey: String,
    appearanceJson: String,
    onPaymentConfirmed: () -> Unit,
    onPaymentError: (String) -> Unit
) {
    Text("Stripe Payment Element não disponível para Desktop.")
}
