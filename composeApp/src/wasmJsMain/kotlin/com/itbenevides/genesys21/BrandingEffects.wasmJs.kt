package com.itbenevides.genesys21

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent

@JsFun("(url) => { let link = document.querySelector(\"link[rel*='icon']\") || document.createElement('link'); link.type = 'image/x-icon'; link.rel = 'shortcut icon'; link.href = url; document.getElementsByTagName('head')[0].appendChild(link); }")
private external fun jsSetFavicon(url: String)

@Composable
actual fun BrandingEffects(page: Page) {
    ThemeScrollbarEffectWrapper()

    val profileImage = page.components.filterIsInstance<PageComponent.ProfileHeader>().firstOrNull()?.imageUrl

    LaunchedEffect(profileImage) {
        if (!profileImage.isNullOrBlank()) {
            jsSetFavicon(profileImage)
        }
    }
}
