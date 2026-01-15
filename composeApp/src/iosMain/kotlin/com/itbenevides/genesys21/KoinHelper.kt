package com.itbenevides.genesys21

import com.itbenevides.genesys21.di.initKoin
import com.itbenevides.genesys21.di.viewModelModule
import org.koin.core.context.stopKoin

fun initKoinIos() {
    println("[DEBUG] Iniciando initKoinIos...")
    
    // Tenta parar qualquer instância prévia para evitar erros de re-inicialização
    try {
        stopKoin()
        println("[DEBUG] Koin parado com sucesso.")
    } catch (e: Exception) {
        println("[DEBUG] Erro ao parar Koin: ${e.message}")
    }
    
    try {
        initKoin {
            modules(viewModelModule)
        }
        println("[DEBUG] Koin inicializado com sucesso com todos os módulos.")
    } catch (e: Exception) {
        println("[DEBUG] ERRO FATAL ao inicializar Koin: ${e.message}")
        e.printStackTrace()
    }
}
