package com.itbenevides.genesys21

import kotlin.test.*

class AuthTest {

    @Test
    fun testAuthRepositoryAvailability() {
        val repository = getAuthRepository()
        assertNotNull(repository, "O repositório de autenticação deve ser inicializado para a plataforma.")
    }

    @Test
    fun testFirebaseAvailabilityOnNativeTargets() {
        // Este teste verifica se o Firebase está disponível. 
        // Em ambientes de CI sem chaves, ele pode retornar false, o que é esperado.
        val available = isFirebaseAvailable()
        println("Firebase disponível nesta plataforma: $available")
    }
}
