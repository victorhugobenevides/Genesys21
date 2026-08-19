package com.itbenevides.genesys21.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun StripeConnectComponent(
    componentName: String,
    publishableKey: String,
    clientSecret: String,
    modifier: Modifier
) {
    Box(modifier = modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        Text("Stripe Connect Embedded Components não suportados em iOS nativo ainda.")
    }
}
