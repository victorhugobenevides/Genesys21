package com.itbenevides.genesys21

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest

class LoginUiTest : KoinTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

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
    fun testLoginScreenElementsAreDisplayed() {
        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodesWithText(GenesysStrings.Welcome).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText(GenesysStrings.EmailLabel).assertExists()
        composeTestRule.onNodeWithText(GenesysStrings.PasswordLabel).assertExists()
        composeTestRule.onNodeWithText(GenesysStrings.LoginButton).assertExists()
    }
}
