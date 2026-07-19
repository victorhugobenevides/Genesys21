package com.itbenevides.genesys21.screenshot

import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.screens.viewer.ServiceBookingScreen
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysSnapshot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class BookingSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    @Test
    fun testServiceBookingScreen() {
        val mockViewModel = mockk<com.itbenevides.genesys21.presentation.PageViewModel>(relaxed = true)
        every { mockViewModel.isLoading } returns MutableStateFlow(false)
        every { mockViewModel.customerName } returns MutableStateFlow("Victor Test")
        every { mockViewModel.customerPhone } returns MutableStateFlow("11999999999")
        every { mockViewModel.appointments } returns MutableStateFlow(emptyList())

        // Mocking available slots for deterministic UI
        val sampleSlots = listOf("09:00", "10:00", "11:00", "14:00", "15:00")
        io.mockk.coEvery {
            mockViewModel.getAvailableSlots(any(), any(), any())
        } returns sampleSlots

        val sampleService = BookingService(
            id = "s1",
            storeId = "store-1",
            name = "Corte de Cabelo Masculino",
            description = "Corte degradê com finalização.",
            price = 45.0,
            durationMinutes = 40
        )
        val samplePage = Page(id = "p1", storeId = "store-1", title = "Barbearia Teste")

        paparazzi.genesysSnapshot {
            ServiceBookingScreen(
                service = sampleService,
                page = samplePage,
                router = mockk(relaxed = true),
                viewModel = mockViewModel
            )
        }
    }
}
