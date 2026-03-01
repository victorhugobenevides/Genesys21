package com.itbenevides.genesys21.navigation

import com.itbenevides.genesys21.MainDispatcherRule
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.PageViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouterTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val viewModel = mockk<PageViewModel>(relaxed = true)

    @Test
    fun `navigateTo should update currentRoute`() {
        val router = Router(viewModel)
        router.navigateTo(Route.PageList)

        assertEquals(Route.PageList, router.currentRoute)
    }

    @Test
    fun `goBack should return to previous route`() {
        val router = Router(viewModel)
        router.navigateTo(Route.PageList)
        router.navigateTo(Route.Login)

        router.goBack()

        assertEquals(Route.PageList, router.currentRoute)
    }

    @Test
    fun `handleDeepLink should navigate to order tracking when url contains order`() = runTest {
        val router = Router(viewModel)

        router.handleDeepLink("genesys21://order/123")
        advanceTimeBy(1000)
        advanceUntilIdle()

        assertTrue(router.currentRoute is Route.OrderTracking)
        assertEquals("123", (router.currentRoute as Route.OrderTracking).orderId)
    }

    @Test
    fun `handleDeepLink should navigate to public viewer when pageId param is present`() = runTest {
        val router = Router(viewModel)
        val page = Page(id = "page1", ownerId = "user", title = "Test", components = emptyList())
        coEvery { viewModel.loadPublicPage("page1") } returns page

        router.handleDeepLink("https://example.com?pageId=page1")
        advanceTimeBy(1000)
        advanceUntilIdle()

        assertTrue(router.currentRoute is Route.PublicViewer)
        assertEquals(page, (router.currentRoute as Route.PublicViewer).page)
    }

    @Test
    fun `handleDeepLink should navigate to PageList when token is available`() = runTest {
        val router = Router(viewModel)
        coEvery { viewModel.getCurrentUserToken() } returns "token"

        router.handleDeepLink(null)
        advanceTimeBy(1000)
        advanceUntilIdle()

        assertEquals(Route.PageList, router.currentRoute)
    }

    @Test
    fun `handleDeepLink should navigate to Login when token is null`() = runTest {
        val router = Router(viewModel)
        coEvery { viewModel.getCurrentUserToken() } returns null

        router.handleDeepLink(null)
        advanceTimeBy(1000)
        advanceUntilIdle()

        assertEquals(Route.Login, router.currentRoute)
    }
}
