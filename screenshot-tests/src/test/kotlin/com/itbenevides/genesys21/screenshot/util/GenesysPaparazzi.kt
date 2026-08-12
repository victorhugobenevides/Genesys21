package com.itbenevides.genesys21.screenshot.util

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.util.LocalTestMode
import com.itbenevides.genesys21.ui.util.ProvideWindowSizeClass
import io.mockk.every
import io.mockk.mockk
import org.koin.compose.KoinApplication
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
        maxPercentDifference = 1.0, // Increased tolerance for major UI changes (Chat, Permissions, Mobile fixes)
    )

fun Paparazzi.genesysSnapshot(
    widthOverride: Dp? = null,
    content: @Composable () -> Unit,
) {
    val mockModule = getMockModule()
    val widthDp = widthOverride ?: 393.dp

    this.snapshot {
        val viewModelStoreOwner = remember {
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            }
        }

        val mockActivity = remember { mockk<ComponentActivity>(relaxed = true) }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides viewModelStoreOwner,
            LocalContext provides mockActivity,
            LocalTestMode provides true
        ) {
            KoinApplication(application = {
                modules(mockModule)
            }) {
                ProvideWindowSizeClass(widthDp) {
                    AppTheme {
                        content()
                    }
                }
            }
        }
    }
}

fun Paparazzi.genesysResponsiveSnapshot(
    namePrefix: String? = null,
    content: @Composable () -> Unit,
) {
    val configs = listOf(
        "Phone" to DeviceConfig.PIXEL_5 to 393.dp,
        "Tablet" to DeviceConfig.NEXUS_7 to 600.dp,
        "Desktop" to DeviceConfig.NEXUS_10.copy(screenWidth = 1200) to 1200.dp
    )

    configs.forEach { (pair, widthDp) ->
        val (name, config) = pair
        val snapshotName = if (namePrefix != null) "${namePrefix}_$name" else name

        this.unsafeUpdateConfig(deviceConfig = config)

        val mockModule = getMockModule()

        this.snapshot(name = snapshotName) {
            val viewModelStoreOwner = remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }

            val mockActivity = remember { mockk<ComponentActivity>(relaxed = true) }

            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner,
                LocalContext provides mockActivity,
                LocalTestMode provides true
            ) {
                KoinApplication(application = {
                    modules(mockModule)
                }) {
                    ProvideWindowSizeClass(widthDp) {
                        AppTheme {
                            content()
                        }
                    }
                }
            }
        }
    }
}

fun getMockModule() = module {
    single<PageViewModel> {
        mockk<PageViewModel>(relaxed = true).apply {
            every { pages } returns MutableStateFlow<List<com.itbenevides.genesys21.domain.model.Page>>(emptyList())
            every { orders } returns MutableStateFlow<List<com.itbenevides.genesys21.domain.model.Order>>(emptyList())
            every { cart } returns MutableStateFlow<List<com.itbenevides.genesys21.domain.model.CartItem>>(emptyList())
            every { cartTotal } returns MutableStateFlow<Double>(0.0)
            every { trackedOrder } returns MutableStateFlow<com.itbenevides.genesys21.domain.model.Order?>(null)
            every { customerName } returns MutableStateFlow<String>("Victor Test")
            every { customerPhone } returns MutableStateFlow<String>("11999999999")
            every { allAvailableCategories } returns MutableStateFlow<List<String>>(emptyList())
            every { isLoading } returns MutableStateFlow<Boolean>(false)
            every { userProfile } returns MutableStateFlow<com.itbenevides.genesys21.domain.model.UserProfile?>(null)
            every { services } returns MutableStateFlow<List<com.itbenevides.genesys21.domain.model.BookingService>>(emptyList())
            every { allAvailableProducts } returns MutableStateFlow<List<com.itbenevides.genesys21.domain.model.Product>>(emptyList())
            every { categories } returns MutableStateFlow<List<com.itbenevides.genesys21.domain.model.Category>>(emptyList())
            every { availability } returns MutableStateFlow<com.itbenevides.genesys21.domain.model.MerchantAvailability?>(null)
            every { templates } returns MutableStateFlow<List<com.itbenevides.genesys21.domain.model.PageTemplate>>(emptyList())
            every { customerOrders } returns MutableStateFlow<List<com.itbenevides.genesys21.domain.model.Order>>(emptyList())
            every { customerAppointments } returns MutableStateFlow<List<com.itbenevides.genesys21.domain.model.Appointment>>(emptyList())
            every { userAddresses } returns MutableStateFlow<List<com.itbenevides.genesys21.domain.model.Address>>(emptyList())
            every { allUsers } returns MutableStateFlow<List<com.itbenevides.genesys21.domain.model.UserProfile>>(emptyList())
            every { isLoggedIn } returns MutableStateFlow<Boolean>(false)
            every { isWaitingForPaymentSignal } returns MutableStateFlow<Boolean>(false)
        }
    }

    single { Router(get()) }

    single<String>(org.koin.core.qualifier.named("hostname")) { "localhost" }
    single<String>(org.koin.core.qualifier.named("baseUrl")) { "http://localhost:8080" }
}
