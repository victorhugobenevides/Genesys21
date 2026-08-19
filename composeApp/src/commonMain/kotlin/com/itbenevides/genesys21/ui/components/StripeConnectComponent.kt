package com.itbenevides.genesys21.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun StripeConnectComponent(
    componentName: String,
    publishableKey: String,
    clientSecret: String,
    modifier: Modifier = Modifier
)
