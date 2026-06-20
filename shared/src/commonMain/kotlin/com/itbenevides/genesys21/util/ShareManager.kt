package com.itbenevides.genesys21.util

/**
 * Interface para compartilhamento nativo multiplataforma.
 */
interface ShareManager {
    /**
     * Abre o diálogo de compartilhamento nativo do sistema.
     * @param title Título do compartilhamento (exibido em alguns sistemas).
     * @param text Texto de descrição.
     * @param url Link a ser compartilhado.
     */
    fun shareLink(
        title: String,
        text: String,
        url: String,
    )
}

/**
 * Singleton de compartilhamento específico para cada plataforma.
 */
expect val ShareManagerInstance: ShareManager
