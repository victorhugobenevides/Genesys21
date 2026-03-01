package com.itbenevides.genesys21

import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.mocks.FakeAuthRepository
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.*

class AuthTest : KoinTest {

    @BeforeTest
    fun setup() {
        stopKoin()
        startKoin {
            modules(module {
                single<AuthRepository> { FakeAuthRepository() }
            })
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testAuthRepositoryAvailability() {
        val repository: AuthRepository by inject()
        assertNotNull(repository, "O repositório de autenticação deve ser inicializado.")
    }
}
