package com.itbenevides.genesys21.screenshot

import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.screens.viewer.ServiceBookingScreen
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.koin.compose.koinInject

class BookingSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    @Test
    fun testServiceBookingScreenResponsive() {
        // We use the koinInject from our helper's mock context if possible,
        // but here we need specific mocks for deterministic behavior.
        // Actually genesysResponsiveSnapshot uses getMockModule() internally.

        val fixedDate = LocalDate(2026, 7, 28)

        val sampleService = BookingService(
            id = "s1",
            storeId = "store-1",
            name = "Corte de Cabelo Masculino",
            description = "Corte degradê com finalização.",
            price = 45.0,
            durationMinutes = 40
        )
        val samplePage = Page(id = "p1", storeId = "store-1", title = "Barbearia Teste")

        paparazzi.genesysResponsiveSnapshot {
            ServiceBookingScreen(
                service = sampleService,
                page = samplePage,
                router = mockk(relaxed = true),
                viewModel = koinInject(),
                today = fixedDate
            )
        }
    }
}
