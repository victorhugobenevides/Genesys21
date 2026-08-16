package com.itbenevides.genesys21.ui.components.organisms.payment

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun StripePaymentElement(
    modifier: Modifier = Modifier,
    clientSecret: String,
    publishableKey: String,
    appearanceJson: String,
    onPaymentConfirmed: () -> Unit,
    onPaymentError: (String) -> Unit
)
