package com.itbenevides.genesys21

import android.app.Application
import com.itbenevides.genesys21.mocks.FakeAuthRepository
import com.itbenevides.genesys21.mocks.FakePageRepository
import com.itbenevides.genesys21.mocks.FakeOrderRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.loadKoinModules

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Inicialização robusta e única para TODO o processo de teste
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@TestApplication)
                modules(createTestModule(FakeAuthRepository(), FakePageRepository(), FakeOrderRepository()))
            }
        }
    }
}
