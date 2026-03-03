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

class LoginFlowTest : KoinTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    
    private val fakeAuth: FakeAuthRepository by inject()

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
    fun testSuccessfulLoginFlow() {
        fakeAuth.shouldSucceed = true
        fakeAuth.setLoggedIn("temp_token")
        runBlocking { fakeAuth.signOut() }

        // CORREÇÃO: Aguarda por EmailLabel, pois "Welcome" não existe na UI atual
        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.EmailLabel).fetchSemanticsNodes().isNotEmpty()
        }

        // CORREÇÃO: Uso de testTags para maior precisão
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@test.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("123456")
        
        composeTestRule.onNodeWithTag("btn_login").performClick()

        // Aguarda transição para Home/Admin
        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.AdminTitle).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText(GenesysStrings.AdminTitle).assertExists()
    }

    @Test
    fun testFailedLoginFlow() {
        fakeAuth.shouldSucceed = false
        fakeAuth.setLoggedIn("temp")
        runBlocking { fakeAuth.signOut() }

        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.EmailLabel).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("email_field").performTextInput("wrong@test.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("000000")
        composeTestRule.onNodeWithTag("btn_login").performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Login falhou", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("Login falhou", ignoreCase = true).assertExists()
    }
}
