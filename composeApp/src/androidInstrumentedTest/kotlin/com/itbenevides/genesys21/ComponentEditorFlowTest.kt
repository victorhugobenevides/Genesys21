package com.itbenevides.genesys21

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.mocks.FakeAuthRepository
import com.itbenevides.genesys21.mocks.FakePageRepository
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject

class ComponentEditorFlowTest : KoinTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    
    private val fakeAuth: FakeAuthRepository by inject()
    private val fakePage: FakePageRepository by inject()

    @Before
    fun setup() {
        TestKoinHelper.startOrReloadKoin()

        fakeAuth.setLoggedIn("test_token_123")
        fakeAuth.shouldSucceed = true
        fakePage.clear()
        
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    private fun startInEditor() {
        val router: Router = get()
        val viewModel: PageViewModel = get()

        composeTestRule.waitUntil(30000) {
            router.currentRoute != Route.Splash
        }
        
        val dummyPage = Page(id = "test_page_id", title = "Página de Teste")
        composeTestRule.runOnUiThread {
            router.navigateTo(Route.WhiteLabel(dummyPage), replace = true)
        }
        
        composeTestRule.waitUntil(30000) {
            !viewModel.isLoading.value && 
            (composeTestRule.onAllNodes(hasTestTag("fab_add_block"), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() ||
             composeTestRule.onAllNodes(hasTestTag("btn_empty_add_block"), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        }
        
        try { androidx.test.espresso.Espresso.closeSoftKeyboard() } catch (e: Exception) {}
    }

    @Test
    fun testAddAndEditMediaComponent() {
        startInEditor()
        openAddComponentMenu()
        
        composeTestRule.waitUntilNodeExists(hasTestTag("catalog_item_Mídia"), 30000)
        composeTestRule.onNodeWithTag("catalog_item_Mídia", useUnmergedTree = true).performClick()
        
        val chipMatcher = hasTestTag("chip_layout_SIDE_TEXT") or hasText("Lado a Lado")
        composeTestRule.waitUntilNodeExists(chipMatcher, 30000)
        
        composeTestRule.onAllNodes(chipMatcher, useUnmergedTree = true)
            .onFirst()
            .performScrollTo()
            .performClick()
        
        composeTestRule.waitUntilNodeExists(hasTestTag("input_media_title"), 15000)
        // Usa performTextReplacement para evitar problemas com limpeza de texto condicional
        composeTestRule.onNodeWithTag("input_media_title", useUnmergedTree = true)
            .performScrollTo()
            .performTextReplacement("Nosso Escritório")
        
        composeTestRule.onNodeWithTag("btn_confirm_media", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        
        composeTestRule.waitUntilNodeExists(hasText("Nosso Escritório"), 30000)
        composeTestRule.onNodeWithText("Nosso Escritório", useUnmergedTree = true).assertExists()
    }

    @Test
    fun testAddAndEditTypographyComponent() {
        startInEditor()
        openAddComponentMenu()
        
        composeTestTagClick("catalog_item_Texto")
        
        composeTestRule.waitUntilNodeExists(hasTestTag("input_typography_text"), 30000)
        
        // CORREÇÃO: Usando performTextReplacement para contornar o bloqueio de limpeza do GenesysTextField
        composeTestRule.onNodeWithTag("input_typography_text", useUnmergedTree = true)
            .performScrollTo()
            .performTextReplacement("Olá Mundo Genesys")
        
        try { androidx.test.espresso.Espresso.closeSoftKeyboard() } catch (e: Exception) {}

        val confirmBtn = composeTestRule.onNodeWithTag("btn_confirm_typography", useUnmergedTree = true)
        confirmBtn.performScrollTo()
        confirmBtn.performClick()
        
        composeTestRule.waitUntil(30000) {
            composeTestRule.onAllNodesWithTag("btn_confirm_typography").fetchSemanticsNodes().isEmpty()
        }
        
        // Verifica se o texto apareceu na lista (ignoreCase para robustez)
        composeTestRule.waitUntilNodeExists(hasText("Olá Mundo Genesys", ignoreCase = true), 30000)
        composeTestRule.onNodeWithText("Olá Mundo Genesys", ignoreCase = true, useUnmergedTree = true).assertExists()
    }

    private fun openAddComponentMenu() {
        val fab = composeTestRule.onAllNodes(hasTestTag("fab_add_block"), useUnmergedTree = true)
        val emptyBtn = composeTestRule.onAllNodes(hasTestTag("btn_empty_add_block"), useUnmergedTree = true)
        
        if (fab.fetchSemanticsNodes().isNotEmpty()) {
            fab.onFirst().performClick()
        } else {
            emptyBtn.onFirst().performClick()
        }
    }

    private fun composeTestTagClick(tag: String) {
        composeTestRule.waitUntilNodeExists(hasTestTag(tag), 30000)
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).performClick()
    }

    private fun ComposeTestRule.waitUntilNodeExists(matcher: SemanticsMatcher, timeoutMillis: Long = 30000) {
        this.waitUntil(timeoutMillis) {
            this.onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
