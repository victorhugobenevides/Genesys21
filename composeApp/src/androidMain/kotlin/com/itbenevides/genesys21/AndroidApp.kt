package com.itbenevides.genesys21

import android.app.Application
import org.koin.core.context.GlobalContext

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Em testes instrumentados, o Koin pode já ter sido iniciado pelo TestRunner
        // ou pela regra de teste. Evitamos reinicializar aqui para não causar crash.
        // A inicialização real ocorre na MainActivity ou nos testes.
    }
}
