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
import org.koin.compose.KoinContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.AppError
import com.itbenevides.genesys21.presentation.UiEvent

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
        maxPercentDifference = 5.0, // Increased tolerance for CI stability
    )

/**
 * Internal implementation to avoid classloader leakage via parameters.
 */
@Composable
private fun GenesysSnapshotContent(
    widthDp: Dp,
    mockUserId: String?,
    mockUserRole: String?,
    mockUserPermissions: String?, // Comma separated
    content: @Composable () -> Unit
) {
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
        val mockModule = module {
            single<PageViewModel> {
                val profile = if (mockUserId != null) {
                    UserProfile(
                        id = mockUserId,
                        email = "test@example.com",
                        name = "Test User",
                        role = mockUserRole?.let { UserRole.valueOf(it) } ?: UserRole.CUSTOMER,
                        permissions = mockUserPermissions?.split(",")?.filter { it.isNotBlank() }?.map { UserPermission.valueOf(it) }?.toSet() ?: emptySet()
                    )
                } else null

                mockk<PageViewModel>(relaxed = true).apply {
                    every { pages } returns MutableStateFlow<List<Page>>(emptyList())
                    every { orders } returns MutableStateFlow<List<Order>>(emptyList())
                    every { cart } returns MutableStateFlow<List<CartItem>>(emptyList())
                    every { cartTotal } returns MutableStateFlow<Double>(0.0)
                    every { cartCount } returns MutableStateFlow<Int>(0)
                    every { trackedOrder } returns MutableStateFlow<Order?>(null)
                    every { customerName } returns MutableStateFlow<String>("Victor Test")
                    every { customerPhone } returns MutableStateFlow<String>("11999999999")
                    every { allAvailableCategories } returns MutableStateFlow<List<String>>(emptyList())
                    every { isLoading } returns MutableStateFlow<Boolean>(false)
                    every { userProfile } returns MutableStateFlow<UserProfile?>(profile)
                    every { services } returns MutableStateFlow<List<BookingService>>(emptyList())
                    every { allAvailableProducts } returns MutableStateFlow<List<Product>>(emptyList())
                    every { categories } returns MutableStateFlow<List<Category>>(emptyList())
                    every { availability } returns MutableStateFlow<MerchantAvailability?>(null)
                    every { templates } returns MutableStateFlow<List<PageTemplate>>(emptyList())
                    every { customerOrders } returns MutableStateFlow<List<Order>>(emptyList())
                    every { customerAppointments } returns MutableStateFlow<List<Appointment>>(emptyList())
                    every { userAddresses } returns MutableStateFlow<List<Address>>(emptyList())
                    every { allUsers } returns MutableStateFlow<List<UserProfile>>(emptyList())
                    every { analytics } returns MutableStateFlow<MerchantAnalytics?>(null)
                    every { appTheme } returns MutableStateFlow<PageThemeConfig>(PageThemeConfig.ELEGANCE)
                    every { isLoggedIn } returns MutableStateFlow<Boolean>(profile != null)
                    every { isWaitingForPaymentSignal } returns MutableStateFlow<Boolean>(false)
                    every { domainMappings } returns MutableStateFlow<List<DomainMapping>>(emptyList())
                    every { chatMessages } returns MutableStateFlow<List<ChatMessage>>(emptyList())
                    every { productSuggestions } returns MutableStateFlow<List<String>>(emptyList())
                    every { categorySuggestions } returns MutableStateFlow<List<String>>(emptyList())
                    every { appointments } returns MutableStateFlow<List<Appointment>>(emptyList())
                    every { upcomingAppointments } returns MutableStateFlow<List<Appointment>>(emptyList())

                    // Flows de eventos
                    every { errorEvents } returns MutableSharedFlow<AppError>()
                    every { uiMessages } returns MutableSharedFlow<String>()
                    every { uiEvent } returns MutableSharedFlow<UiEvent>()
                }
            }

            single { Router(get()) }

            single<String>(org.koin.core.qualifier.named("hostname")) { "localhost" }
            single<String>(org.koin.core.qualifier.named("baseUrl")) { "http://localhost:8080" }
        }

        val koin = remember {
            koinApplication {
                modules(mockModule)
            }.koin
        }

        KoinContext(context = koin) {
            ProvideWindowSizeClass(widthDp) {
                AppTheme {
                    content()
                }
            }
        }
    }
}

/**
 * Standard snapshot.
 */
fun genesysSnapshot(
    paparazzi: Paparazzi,
    content: @Composable () -> Unit
) {
    paparazzi.snapshot {
        GenesysSnapshotContent(393.dp, null, null, null, content)
    }
}

/**
 * Responsive snapshot for all devices.
 */
fun genesysResponsiveSnapshot(
    paparazzi: Paparazzi,
    content: @Composable () -> Unit
) {
    genesysResponsiveSnapshotFull(paparazzi, null, null, null, null, content)
}

/**
 * Responsive snapshot with name prefix.
 */
fun genesysResponsiveSnapshotWithPrefix(
    paparazzi: Paparazzi,
    namePrefix: String,
    content: @Composable () -> Unit
) {
    genesysResponsiveSnapshotFull(paparazzi, namePrefix, null, null, null, content)
}

/**
 * Full control responsive snapshot.
 */
fun genesysResponsiveSnapshotFull(
    paparazzi: Paparazzi,
    namePrefixOrNull: String?,
    mockUserIdOrNull: String?,
    mockUserRoleOrNull: String?,
    mockUserPermissionsOrNull: String?,
    content: @Composable () -> Unit
) {
    val configs = listOf(
        "phone" to DeviceConfig.PIXEL_5 to 393.dp,
        "tablet" to DeviceConfig.NEXUS_7 to 600.dp,
        "desktop" to DeviceConfig.NEXUS_10.copy(screenWidth = 1200) to 1200.dp
    )

    configs.forEach { (pair, widthDp) ->
        val (name, config) = pair
        val snapshotName = if (namePrefixOrNull != null) "${namePrefixOrNull}_$name" else name

        paparazzi.unsafeUpdateConfig(deviceConfig = config)
        paparazzi.snapshot(name = snapshotName) {
            GenesysSnapshotContent(widthDp, mockUserIdOrNull, mockUserRoleOrNull, mockUserPermissionsOrNull, content)
        }
    }
}
