package com.itbenevides.genesys21

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.mocks.FakeAuthRepository
import com.itbenevides.genesys21.mocks.FakePageRepository
import com.itbenevides.genesys21.mocks.FakeOrderRepository
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject

class ProductDetailsUiTest : KoinTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private val fakeAuth = FakeAuthRepository()
    private val fakePage = FakePageRepository()
    private val testModule = createTestModule(fakeAuth, fakePage)

    private val testProduct = Product(
        id = "p123",
        name = "Produto Especial",
        price = 199.90,
        description = "Uma descrição detalhada do produto.",
        stock = 10,
        imageUrls = listOf("https://fake.url/img.jpg")
    )

    @Before
    fun setup() {
        TestKoinHelper.startOrReloadKoin()
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    @Test
    fun testProductInfoIsDisplayedCorrectly() {
        val router: Router = get()
        
        composeTestRule.waitUntil(25000) {
            router.currentRoute != Route.Splash
        }

        composeTestRule.runOnUiThread {
            router.navigateTo(Route.ProductDetails(testProduct, Route.PageList))
        }

        // Aguarda o carregamento do conteúdo
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Produto Especial").fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("Produto Especial").assertExists()
        
        // CORREÇÃO: Usamos testTag para evitar ambiguidade entre o preço do header e do footer
        composeTestRule.onNodeWithTag("product_price")
            .assertTextContains("R$ 199.9", substring = true)
            .assertExists()

        composeTestRule.onNodeWithText("Uma descrição detalhada do produto.").assertExists()
    }

    @Test
    fun testAddToCartFlow() {
        val router: Router = get()
        
        composeTestRule.waitUntil(25000) {
            router.currentRoute != Route.Splash
        }

        composeTestRule.runOnUiThread {
            router.navigateTo(Route.ProductDetails(testProduct, Route.PageList))
        }

        // Aguarda botão de adicionar estar visível
        composeTestRule.waitUntil(15000) {
             composeTestRule.onAllNodesWithTag("btn_sticky_add_to_cart").fetchSemanticsNodes().isNotEmpty()
        }

        // Clica para adicionar ao carrinho usando a TAG específica
        composeTestRule.onNodeWithTag("btn_sticky_add_to_cart").performClick()

        // Aguarda pela mensagem do Snackbar
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("adicionado ao carrinho", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        // Clica na ação "Ver Carrinho" do Snackbar
        composeTestRule.onNodeWithText("Ver Carrinho", ignoreCase = true).performClick()

        // Valida se navegou para o carrinho
        composeTestRule.waitUntil(10000) {
            router.currentRoute is Route.Cart
        }
    }

    @Test
    fun testOutOfStockProductDisablesButton() {
        val router: Router = get()
        val outOfStockProduct = testProduct.copy(stock = 0)
        
        composeTestRule.waitUntil(25000) {
            router.currentRoute != Route.Splash
        }

        composeTestRule.runOnUiThread {
            router.navigateTo(Route.ProductDetails(outOfStockProduct, Route.PageList))
        }

        // Verifica se o botão de adicionar está desabilitado usando a TAG
        composeTestRule.onNodeWithTag("btn_sticky_add_to_cart").assertIsNotEnabled()
        
        // Verifica se o badge de esgotado aparece
        composeTestRule.onNodeWithText("Esgotado", ignoreCase = true).assertExists()
    }
}
