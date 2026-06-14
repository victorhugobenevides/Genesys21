package com.itbenevides.genesys21.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.components.feedback.GenesysLoadingIndicator
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextAlign
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings

/**
 * SplashScreen padronizada com o Design System Genesys21.
 */
@Composable
fun SplashScreen() {
    GenesysColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = GenesysAlignment.Center,
        verticalArrangement = Arrangement.Center,
        usePadding = true,
    ) {
        // Ícone representativo usando o padrão do sistema
        Icon(
            imageVector = GenesysIcons.Magic,
            contentDescription = null,
            modifier = Modifier.size(GenesysSpacing.Huge.value * 2),
            tint = MaterialTheme.colorScheme.primary,
        )

        GenesysSpacer(GenesysSpacing.Large)

        // Nome da marca usando GenesysText
        GenesysText(
            text = GenesysStrings.AppName,
            style = GenesysTextStyle.Headline,
            fontWeight = GenesysFontWeight.ExtraBold,
            textAlign = GenesysTextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
        )

        GenesysSpacer(GenesysSpacing.ExtraLarge)

        // Indicador de carregamento padronizado do sistema
        GenesysLoadingIndicator()
    }
}
