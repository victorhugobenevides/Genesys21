package com.itbenevides.genesys21

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.itbenevides.genesys21.domain.model.Category
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.mocks.FakeAuthRepository
import com.itbenevides.genesys21.mocks.FakePageRepository
import com.itbenevides.genesys21.mocks.FakeOrderRepository
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

class ProductEditorFlowTest : KoinTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    
    private val fakeAuth: FakeAuthRepository by inject()
    private val fakePage: FakePageRepository by inject()

    private val testProduct = Product(id = "p1", name = "Produto Antigo", price = 10.0, stock = 5)
    private val testPage = Page(
        id = "page1", 
        title = "Loja", 
        ownerId = "test_token",
        components = listOf(PageComponent.ProductList(products = listOf(testProduct)))
    )

    @Before
    fun setup() {
        // Reinicialização total do ambiente Koin para isolamento
        TestKoinHelper.startOrReloadKoin()
        
        fakeAuth.setLoggedIn("test_token")
        
        runBlocking {
            fakePage.clear()
            // Salva dados iniciais para o ViewModel não travar em loading
            fakePage.savePage(testPage, "test_token", false)
            fakePage.saveCategory(Category(id = 1, name = "Geral", ownerId = "test_token"), "test_token")
        }

        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    @Test
    fun testEditProductAndSaveFlow() {
        val router: Router = get()
        val viewModel: PageViewModel = get()
        
        // 1. Aguarda estabilização inicial (Sair da Splash)
        composeTestRule.waitUntil(30000) {
            router.currentRoute != Route.Splash
        }

        // 2. Navega para o editor
        composeTestRule.runOnUiThread {
            // Força a ViewModel a carregar os dados do mock antes de entrar no editor
            viewModel.loadPages()
            router.navigateTo(Route.ProductEditor(testPage, testProduct, 0))
        }

        // 3. Aguarda o editor renderizar (Título "Editar Detalhes")
        composeTestRule.waitUntil(30000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.EditProduct, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }

        // 4. Edita o nome usando substituição completa (performTextReplacement)
        // Procuramos por qualquer nó que tenha o texto do produto antigo
        composeTestRule.onNodeWithText("Produto Antigo", useUnmergedTree = true)
            .performTextReplacement("Produto Novo")
        
        // 5. Salva usando o texto da TopAppBar
        composeTestRule.onNodeWithText(GenesysStrings.Save, ignoreCase = true).performClick()

        // 6. Verifica se navegou de volta (saiu da tela de edição)
        composeTestRule.waitUntil(20000) {
            router.currentRoute !is Route.ProductEditor
        }
    }
}
