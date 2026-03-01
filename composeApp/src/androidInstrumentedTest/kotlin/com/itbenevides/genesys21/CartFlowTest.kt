package com.itbenevides.genesys21

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.mocks.FakeCartRepository
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject

class CartFlowTest : KoinTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private val fakeCart: FakeCartRepository by inject()

    private val testProduct = Product(id = "p1", name = "Produto Teste", price = 100.0, stock = 10)
    private val testPage = Page(id = "page1", title = "Loja", ownerId = "owner")

    @Before
    fun setup() {
        TestKoinHelper.startOrReloadKoin()
        
        runBlocking {
            fakeCart.clearCart()
            fakeCart.addToCart(CartItem(testProduct, 1))
        }

        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    @Test
    fun testCartQuantityManipulation() {
        val router: Router = get()

        composeTestRule.waitUntil(30000) { router.currentRoute != Route.Splash }

        composeTestRule.runOnUiThread {
            router.navigateTo(Route.Cart(testPage))
        }

        // Verifica item inicial usando Tag para evitar ambiguidade com o Total
        composeTestRule.onNodeWithTag("cart_item_price_p1", useUnmergedTree = true).assertExists()
        
        // Verifica o Total usando Tag específica (Resolvendo o erro de "found 2 nodes")
        composeTestRule.onNodeWithTag("cart_total_price", useUnmergedTree = true)
            .assertTextContains("100", substring = true)

        // Aumenta quantidade
        composeTestRule.onNodeWithTag("btn_quantity_increase").performClick()
        
        // Verifica total atualizado (200) - Espera o estado refletir na UI
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasTestTag("cart_total_price") and hasText("200", substring = true), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // Diminui quantidade
        composeTestRule.onNodeWithTag("btn_quantity_decrease").performClick()
        
        // Verifica volta ao total original
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasTestTag("cart_total_price") and hasText("100", substring = true), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
