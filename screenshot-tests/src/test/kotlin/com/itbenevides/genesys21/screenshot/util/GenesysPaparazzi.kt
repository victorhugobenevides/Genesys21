package com.itbenevides.genesys21.screenshot.util

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.runtime.Composable
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.di.initKoin
import com.itbenevides.genesys21.di.viewModelModule
import org.koin.core.context.GlobalContext
import org.koin.compose.KoinContext
import com.itbenevides.genesys21.domain.usecase.*
import org.koin.dsl.module
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.navigation.Router

/**
 * Standard Paparazzi configuration for Genesys21 Design System tests.
 */
fun createGenesysPaparazzi(
    deviceConfig: DeviceConfig = DeviceConfig.PIXEL_5,
    theme: String = "android:Theme.Material.Light.NoActionBar"
): Paparazzi = Paparazzi(
    deviceConfig = deviceConfig,
    theme = theme
)

/**
 * Helper to snapshot a component wrapped in the standard AppTheme and Koin context.
 */
fun Paparazzi.genesysSnapshot(
    content: @Composable () -> Unit
) {
    // Ensure Koin is started with a mock platform module and necessary business logic
    if (GlobalContext.getOrNull() == null) {
        val mockModule = module {
            // Basic platform mocks needed by ViewModel
            single<String>(org.koin.core.qualifier.named("hostname")) { "localhost" }
            single<String>(org.koin.core.qualifier.named("baseUrl")) { "http://localhost:8080" }

            // Re-use real ViewModel module but ensure its dependencies are satisfied
            // In a real project, we would use a more robust Mocking strategy here
            // but for a quick fix, satisfying the graph is key.
        }

        try {
            initKoin(additionalModules = listOf(viewModelModule, mockModule)) { }
        } catch (e: Exception) {
            // Already started or conflict
        }
    }

    this.snapshot {
        KoinContext {
            AppTheme {
                content()
            }
        }
    }
}
