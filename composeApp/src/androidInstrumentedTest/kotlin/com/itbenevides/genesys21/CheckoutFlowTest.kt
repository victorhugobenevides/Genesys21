package com.itbenevides.genesys21

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.PageRepository
import com.itbenevides.genesys21.mocks.FakePageRepository
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject

class CheckoutFlowTest : KoinTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private val fakePage: PageRepository by inject()

    private val testProduct = Product(
        id = "p1",
        name = "Produto Teste",
        price = 50.0,
        stock = 10
    )
    
    private val testPage = Page(
        id = "page1",
        title = "Vitrine de Teste",
        ownerId = "owner",
        components = listOf(
            PageComponent.ProductList(
                products = listOf(testProduct),
                customLabel = "Nossos Produtos"
            )
        )
    )

    @Before
    fun setup() {
        TestKoinHelper.startOrReloadKoin()
        
        (fakePage as? FakePageRepository)?.clear()
        runBlocking {
            fakePage.savePage(testPage, "owner", false)
        }

        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    @Test
    fun testAddToCartAndCheckoutFlow() {
        val router: Router = get()

        composeTestRule.waitUntil(20000) {
            router.currentRoute != Route.Splash
        }

        composeTestRule.runOnUiThread {
            router.navigateTo(Route.PublicViewer(testPage))
        }

        // 1. Seleciona Produto
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Produto Teste").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Produto Teste").performClick()
        
        // 2. Adiciona ao Carrinho
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.AddToCartAction, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(GenesysStrings.AddToCartAction, ignoreCase = true).performClick()
        
        // 3. Vai para o Carrinho
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.ViewCart, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(GenesysStrings.ViewCart, ignoreCase = true).performClick()

        // 4. Preenche Identificação (Nome e Telefone)
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.CartTitle, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("Cliente Especial")
        composeTestRule.onAllNodes(hasSetTextAction()).onLast().performTextInput("11988887777")
        
        // 5. Finaliza
        composeTestRule.onNodeWithText("Finalizar via WhatsApp", ignoreCase = true).performClick()
        
        // 6. Verifica Sucesso (Navegação para Tracking)
        composeTestRule.waitUntil(30000) {
            router.currentRoute is Route.OrderTracking
        }
        
        // CORREÇÃO: O título real na UI é "Acompanhamento"
        composeTestRule.onNodeWithText(GenesysStrings.TrackOrderTitle, ignoreCase = true).assertExists()
    }
}
