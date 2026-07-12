package com.itbenevides.genesys21.screenshot.util

import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.itbenevides.genesys21.di.viewModelModule
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.theme.AppTheme
import io.mockk.every
import io.mockk.mockk
import org.koin.compose.KoinContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Standard Paparazzi configuration for Genesys21 Design System tests.
 */
fun createGenesysPaparazzi(
    deviceConfig: DeviceConfig = DeviceConfig.PIXEL_5,
    theme: String = "android:Theme.Material.Light.NoActionBar",
): Paparazzi =
    Paparazzi(
        deviceConfig = deviceConfig,
        theme = theme,
    )

/**
 * Helper to snapshot a component wrapped in the standard AppTheme and Koin context.
 */
fun Paparazzi.genesysSnapshot(
    content: @Composable () -> Unit,
) {
    if (GlobalContext.getOrNull() != null) {
        stopKoin()
    }

    val mockModule =
        module {
            single<PageViewModel> {
                mockk<PageViewModel>(relaxed = true).apply {
                    every { pages } returns MutableStateFlow(emptyList())
                    every { orders } returns MutableStateFlow(emptyList())
                    every { cart } returns MutableStateFlow(emptyList())
                    every { cartTotal } returns MutableStateFlow(0.0)
                    every { trackedOrder } returns MutableStateFlow(null)
                    every { customerName } returns MutableStateFlow("")
                    every { customerPhone } returns MutableStateFlow("")
                    every { allAvailableCategories } returns MutableStateFlow(emptyList())
                    every { isLoading } returns MutableStateFlow(false)
                }
            }

            single { Router(get()) }

            single<String>(org.koin.core.qualifier.named("hostname")) { "localhost" }
            single<String>(org.koin.core.qualifier.named("baseUrl")) { "http://localhost:8080" }
        }

    startKoin {
        modules(viewModelModule, mockModule)
    }

    this.snapshot {
        KoinContext {
            AppTheme {
                content()
            }
        }
    }
}
