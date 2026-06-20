package com.itbenevides.genesys21

import androidx.compose.runtime.Composable
import com.itbenevides.genesys21.domain.model.Page

@Composable
actual fun BrandingEffects(page: Page) {
    ThemeScrollbarEffectWrapper()
}
