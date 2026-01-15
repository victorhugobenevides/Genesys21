package com.itbenevides.genesys21.presentation

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.usecase.GetPagesUseCase
import com.itbenevides.genesys21.domain.usecase.SavePageUseCase
import com.itbenevides.genesys21.mocks.FakeAuthRepository
import com.itbenevides.genesys21.mocks.FakePageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class PageViewModelTest {

    private lateinit var viewModel: PageViewModel
    private lateinit var fakePageRepository: FakePageRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository
    
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        fakePageRepository = FakePageRepository()
        fakeAuthRepository = FakeAuthRepository()
        
        val getPagesUseCase = GetPagesUseCase(fakePageRepository)
        val savePageUseCase = SavePageUseCase(fakePageRepository)
        
        viewModel = PageViewModel(getPagesUseCase, savePageUseCase, fakeAuthRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPages should update state with list from repository`() = runTest {
        val testPage = Page("1", "Teste ViewModel")
        fakePageRepository.savePage(testPage, "token")
        
        viewModel.loadPages()
        
        // No StandardTestDispatcher, precisamos avançar o tempo/execução
        advanceUntilIdle()
        
        val pages = viewModel.pages.value
        assertEquals(1, pages.size)
        assertEquals("Teste ViewModel", pages[0].title)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `signIn success should call onSuccess callback`() = runTest {
        var successCalled = false
        
        viewModel.signIn("test@test.com", "password", 
            onSuccess = { successCalled = true },
            onFailure = { }
        )
        
        advanceUntilIdle()
        assertTrue(successCalled)
    }

    @Test
    fun `signIn failure should call onFailure callback with message`() = runTest {
        fakeAuthRepository.shouldReturnError = true
        var failureMessage = ""
        
        viewModel.signIn("test@test.com", "wrong", 
            onSuccess = { },
            onFailure = { failureMessage = it }
        )
        
        advanceUntilIdle()
        assertEquals("Login falhou", failureMessage)
    }

    @Test
    fun `savePage should refresh list after success`() = runTest {
        val newPage = Page("new", "Nova")
        
        var completeCalled = false
        viewModel.savePage(newPage, isEditing = false) {
            completeCalled = true
        }
        
        advanceUntilIdle()
        
        assertTrue(completeCalled)
        assertEquals(1, viewModel.pages.value.size)
    }
}
