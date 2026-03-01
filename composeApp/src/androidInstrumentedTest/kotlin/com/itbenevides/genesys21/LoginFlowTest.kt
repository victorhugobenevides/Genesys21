package com.itbenevides.genesys21

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.itbenevides.genesys21.mocks.FakeAuthRepository
import com.itbenevides.genesys21.mocks.FakePageRepository
import com.itbenevides.genesys21.mocks.FakeOrderRepository
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject

class LoginFlowTest : KoinTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    
    private val fakeAuth: FakeAuthRepository by inject()

    @Before
    fun setup() {
        stopKoin()
        startKoin {
            modules(createTestModule(FakeAuthRepository(), FakePageRepository(), FakeOrderRepository()))
        }
        
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        stopKoin()
    }

    @Test
    fun testSuccessfulLoginFlow() {
        fakeAuth.shouldSucceed = true
        fakeAuth.setLoggedIn("temp_token")
        runBlocking { fakeAuth.signOut() }

        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.Welcome).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("test@test.com")
        composeTestRule.onAllNodes(hasSetTextAction()).onLast().performTextInput("123456")
        
        composeTestRule.onNodeWithText(GenesysStrings.LoginButton).performClick()

        composeTestRule.waitUntil(15000) {
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
            composeTestRule.onAllNodesWithText(GenesysStrings.Welcome).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("wrong@test.com")
        composeTestRule.onAllNodes(hasSetTextAction()).onLast().performTextInput("000000")
        composeTestRule.onNodeWithText(GenesysStrings.LoginButton).performClick()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Login falhou", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("Login falhou", ignoreCase = true).assertExists()
    }
}
