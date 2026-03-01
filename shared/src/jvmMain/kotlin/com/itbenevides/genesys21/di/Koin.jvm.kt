package com.itbenevides.genesys21.di

import org.koin.dsl.module

actual fun platformModule() = module {
    // Dependências específicas da JVM (se houver)
}

actual fun getCurrentUrl(): String? {
    // No contexto do servidor JVM, não há uma "URL atual" do cliente.
    // Retornar null é a implementação correta.
    return null
}
