package com.itbenevides.genesys21.util

@JsFun("(title, text, url) => { if (navigator.share) { navigator.share({ title: title, text: text, url: url }).catch(console.error); } else { window.prompt('Copie o link:', url); } }")
private external fun jsNativeShare(
    title: String,
    text: String,
    url: String,
)

/**
 * Implementação Web usando a Web Share API.
 * Fallback para prompt caso não suportado (ex: Chrome Desktop).
 */
actual val ShareManagerInstance: ShareManager =
    object : ShareManager {
        override fun shareLink(
            title: String,
            text: String,
            url: String,
        ) {
            jsNativeShare(title, text, url)
        }
    }
