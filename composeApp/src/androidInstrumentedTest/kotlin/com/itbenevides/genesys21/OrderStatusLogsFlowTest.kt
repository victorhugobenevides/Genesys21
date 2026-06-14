package com.itbenevides.genesys21

import android.content.Intent
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.mocks.FakeOrderRepository
import com.itbenevides.genesys21.mocks.TestKoinHelper
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import kotlinx.coroutines.runBlocking

class OrderStatusLogsFlowTest : KoinTest {
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    private val fakeOrder: FakeOrderRepository by inject()

    private val testOrderId = "order-logs-test-123"

    @Before
    fun setup() {
        TestKoinHelper.startOrReloadKoin()

        val order =
            Order(
                id = testOrderId,
                userId = "owner",
                customerId = "fake-session",
                total = 150.0,
                status = OrderStatus.PENDING,
                items = emptyList(),
                createdAt = System.currentTimeMillis(),
            )

        runBlocking {
            fakeOrder.createOrder(order)
            // Atualiza status para criar logs
            fakeOrder.updateOrderStatus("owner", testOrderId, OrderStatus.PROCESSING)
        }

        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    @Test
    fun testOrderTrackingDisplaysStatusHistory() {
        val router: Router = get()
        val viewModel: PageViewModel = get()

        composeTestRule.waitUntil(20000) {
            router.currentRoute != Route.Splash
        }

        composeTestRule.runOnUiThread {
            router.navigateTo(Route.OrderTracking(testOrderId))
        }

        // Aguarda a exibição da tela de rastreamento do pedido
        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodesWithText("Histórico do Pedido").fetchSemanticsNodes().isNotEmpty()
        }

        // Verifica que o histórico do pedido é exibido com os status corretos
        composeTestRule.onNodeWithText("Histórico do Pedido").assertExists()
        composeTestRule.onNodeWithText("Pedido Recebido").assertExists()
        composeTestRule.onNodeWithText("Em Preparação").assertExists()
    }
}
