package com.itbenevides.genesys21.di

/**
 * Implementação para JVM (Servidor).
 * Retorna uma string padrão já que o conceito de "hostname do navegador" não existe no servidor.
 */
actual fun getHostname(): String = "localhost"
