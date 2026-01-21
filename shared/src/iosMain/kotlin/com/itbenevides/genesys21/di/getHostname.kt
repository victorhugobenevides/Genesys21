package com.itbenevides.genesys21.di

/**
 * Implementação para iOS.
 * No mobile, não temos o conceito de "hostname do navegador", então retornamos um padrão.
 */
actual fun getHostname(): String = "ios"
