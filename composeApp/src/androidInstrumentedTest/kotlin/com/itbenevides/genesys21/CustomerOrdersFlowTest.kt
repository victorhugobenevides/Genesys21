package com.itbenevides.genesys21

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.OrderRepository
import com.itbenevides.genesys21.mocks.*
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject

class CustomerOrdersFlowTest : KoinTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    
    private val fakeAuth: FakeAuthRepository by inject()
    private val fakeOrder: FakeOrderRepository by inject()
    private val fakeCart: FakeCartRepository by inject()

    // ID de 6 caracteres para garantir visibilidade total na UI (que usa takeLast(6))
    private val testOrderId = "123456"

    @Before
    fun setup() {
        TestKoinHelper.startOrReloadKoin()

        fakeAuth.setLoggedIn("customer_token")
        fakeCart.setSessionId("fake-session")
        
        val order = Order(
            id = testOrderId,
            userId = "owner",
            customerId = "fake-session",
            total = 100.0,
            status = OrderStatus.COMPLETED,
            items = emptyList(),
            createdAt = System.currentTimeMillis()
        )
        runBlocking {
            fakeOrder.createOrder(order)
        }

        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    @Test
    fun testCustomerCanSeeOrderHistory() {
        val router: Router = get()
        val viewModel: PageViewModel = get()

        composeTestRule.waitUntil(20000) {
            router.currentRoute != Route.Splash
        }

        composeTestRule.runOnUiThread {
            viewModel.loadCustomerOrders()
            router.navigateTo(Route.CustomerOrderHistory)
        }

        // Aguarda a lista ser populada. Buscamos pelo ID que sabemos que aparece (os 6 dígitos)
        composeTestRule.waitUntil(30000) {
            composeTestRule.onAllNodesWithText(testOrderId, substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText(GenesysStrings.OrderHistoryTitle, ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText(testOrderId, substring = true).assertExists()
    }
}
