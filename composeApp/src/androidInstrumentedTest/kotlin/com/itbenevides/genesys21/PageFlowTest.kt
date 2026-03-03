package com.itbenevides.genesys21

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.itbenevides.genesys21.mocks.FakeAuthRepository
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.inject

class PageFlowTest : KoinTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private val fakeAuth: FakeAuthRepository by inject()

    @Before
    fun setup() {
        TestKoinHelper.startOrReloadKoin()
        
        fakeAuth.shouldSucceed = true
        fakeAuth.setLoggedIn("temp_token") 
        runBlocking { fakeAuth.signOut() }

        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    @Test
    fun testCreateNewPageFlow() {
        // 1. Fluxo de Login - Aguarda EmailLabel em vez de Welcome
        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.EmailLabel).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("email_field").performTextInput("admin@test.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("123456")
        composeTestRule.onNodeWithTag("btn_login").performClick()

        // 2. Aguarda a tela de Administração carregar
        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.AdminTitle).fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Clica no botão de adicionar (+)
        composeTestRule.onNodeWithTag("btn_create_page", useUnmergedTree = true).performClick()
        
        // 4. Preenchimento do diálogo de nova página
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.NewPageTitle).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithTag("input_new_page_title").performTextInput("Minha Loja de Teste")
        composeTestRule.onNodeWithTag("btn_confirm_create_page").performClick()
        
        // 5. Verifica se a página foi criada e aparece na lista
        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodesWithText("Minha Loja de Teste").fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("Minha Loja de Teste").assertExists()
    }
}
