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
        // Carrega o módulo ANTES de lançar a Activity
        loadKoinModules(createTestModule(FakeAuthRepository(), FakePageRepository(), FakeOrderRepository()))
        
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        unloadKoinModules(testModule)
    }

    @Test
    fun testProductInfoIsDisplayedCorrectly() {
        val router: Router = get()
        
        composeTestRule.waitUntil(20000) {
            router.currentRoute != Route.Splash
        }

        // Navega diretamente para a tela de detalhes (simulando a rota)
        composeTestRule.runOnUiThread {
            router.navigateTo(Route.ProductDetails(testProduct, Route.PageList))
        }

        // Tenta encontrar por Tag ou Texto
        composeTestRule.onNode(hasTestTag("product_title") or hasText("Produto Especial")).assertExists()
        composeTestRule.onNode(hasTestTag("product_price") or hasText("199,90", substring = true)).assertExists()
        composeTestRule.onNodeWithText("Uma descrição detalhada do produto.").assertExists()
    }

    @Test
    fun testAddToCartFlow() {
        val router: Router = get()
        
        composeTestRule.waitUntil(20000) {
            router.currentRoute != Route.Splash
        }

        composeTestRule.runOnUiThread {
            router.navigateTo(Route.ProductDetails(testProduct, Route.PageList))
        }

        // Clica para adicionar ao carrinho
        composeTestRule.onNode(hasTestTag("btn_add_to_cart") or hasText("Adicionar", substring = true))
            .performClick()

        // Aguarda o diálogo de sucesso aparecer
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.AddedToCartTitle).fetchSemanticsNodes().isNotEmpty()
        }

        // Verifica se o diálogo de sucesso está visível
        composeTestRule.onNodeWithText(GenesysStrings.AddedToCartTitle).assertExists()

        // Clica em "Ver Carrinho" no diálogo
        composeTestRule.onNode(hasTestTag("btn_dialog_view_cart") or hasText("Ver Carrinho", ignoreCase = true))
            .performClick()

        // Valida se navegou para o carrinho (verificando a rota atual ou um elemento do carrinho)
        composeTestRule.waitUntil(5000) {
            router.currentRoute is Route.Cart
        }
    }

    @Test
    fun testOutOfStockProductDisablesButton() {
        val router: Router = get()
        val outOfStockProduct = testProduct.copy(stock = 0)
        
        composeTestRule.waitUntil(20000) {
            router.currentRoute != Route.Splash
        }

        composeTestRule.runOnUiThread {
            router.navigateTo(Route.ProductDetails(outOfStockProduct, Route.PageList))
        }

        // Verifica se o botão de adicionar está desabilitado
        composeTestRule.onNode(hasTestTag("btn_add_to_cart") or hasText("Adicionar", substring = true))
            .assertIsNotEnabled()
        
        // Verifica se o badge de esgotado aparece
        composeTestRule.onNodeWithText("Esgotado", ignoreCase = true).assertExists()
    }
}
