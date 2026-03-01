package com.itbenevides.genesys21

import androidx.test.platform.app.InstrumentationRegistry
import com.itbenevides.genesys21.mocks.FakeAuthRepository
import com.itbenevides.genesys21.mocks.FakeOrderRepository
import com.itbenevides.genesys21.mocks.FakePageRepository
import com.itbenevides.genesys21.util.Analytics
import io.mockk.every
import io.mockk.mockkObject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

object TestKoinHelper {
    fun startOrReloadKoin() {
        // Mock do Analytics para evitar crash do Firebase em todos os testes instrumentados
        try {
            mockkObject(Analytics)
            every { Analytics.logEvent(any(), any()) } returns Unit
            every { Analytics.trackPageView(any()) } returns Unit
            every { Analytics.logException(any(), any(), any()) } returns Unit
        } catch (e: Exception) { }

        // REINICIALIZAÇÃO TOTAL: Para o Koin se ele estiver rodando
        if (GlobalContext.getOrNull() != null) {
            stopKoin()
        }
        
        startKoin {
            androidContext(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext)
            // IMPORTANTE: Criamos novas instâncias de mocks para isolamento total
            modules(createTestModule(FakeAuthRepository(), FakePageRepository(), FakeOrderRepository()))
        }
    }
}
