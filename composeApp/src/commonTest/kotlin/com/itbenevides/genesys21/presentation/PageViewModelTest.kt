package com.itbenevides.genesys21.presentation

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.usecase.DeleteCategoryUseCase
import com.itbenevides.genesys21.domain.usecase.DeletePageUseCase
import com.itbenevides.genesys21.domain.usecase.GetCategoriesUseCase
import com.itbenevides.genesys21.domain.usecase.GetCustomerOrdersUseCase
import com.itbenevides.genesys21.domain.usecase.GetFirstPublicPageUseCase
import com.itbenevides.genesys21.domain.usecase.GetOrderByIdUseCase
import com.itbenevides.genesys21.domain.usecase.GetOrdersUseCase
import com.itbenevides.genesys21.domain.usecase.GetPageByDomainUseCase
import com.itbenevides.genesys21.domain.usecase.GetPagesUseCase
import com.itbenevides.genesys21.domain.usecase.GetPublicPageUseCase
import com.itbenevides.genesys21.domain.usecase.SaveCategoryUseCase
import com.itbenevides.genesys21.domain.usecase.SavePageUseCase
import com.itbenevides.genesys21.domain.usecase.SubmitOrderUseCase
import com.itbenevides.genesys21.domain.usecase.UpdateOrderStatusUseCase
import com.itbenevides.genesys21.domain.usecase.UploadImageUseCase
import com.itbenevides.genesys21.mocks.FakeAuthRepository
import com.itbenevides.genesys21.mocks.FakePageDraftRepository
import com.itbenevides.genesys21.mocks.FakePageRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class PageViewModelTest {
    private lateinit var viewModel: PageViewModel
    private lateinit var fakePageRepository: FakePageRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakePageDraftRepository: FakePageDraftRepository
    private lateinit var fakeCartRepository: FakeCartRepository
    private lateinit var fakeCustomerRepository: FakeCustomerRepository
    private lateinit var fakeOrderRepository: FakeOrderRepository

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakePageRepository = FakePageRepository()
        fakeAuthRepository = FakeAuthRepository()
        fakePageDraftRepository = FakePageDraftRepository()
        fakeCartRepository = FakeCartRepository()
        fakeCustomerRepository = FakeCustomerRepository()
        fakeOrderRepository = FakeOrderRepository()

        viewModel =
            PageViewModel(
                getPagesUseCase = GetPagesUseCase(fakePageRepository),
                savePageUseCase = SavePageUseCase(fakePageRepository),
                deletePageUseCase = DeletePageUseCase(fakePageRepository),
                getPublicPageUseCase = GetPublicPageUseCase(fakePageRepository),
                getPageByDomainUseCase = GetPageByDomainUseCase(fakePageRepository),
                getFirstPublicPageUseCase = GetFirstPublicPageUseCase(fakePageRepository),
                uploadImageUseCase = UploadImageUseCase(fakePageRepository),
                getOrdersUseCase = GetOrdersUseCase(fakeOrderRepository),
                getCustomerOrdersUseCase = GetCustomerOrdersUseCase(fakeOrderRepository),
                getOrderByIdUseCase = GetOrderByIdUseCase(fakeOrderRepository),
                submitOrderUseCase = SubmitOrderUseCase(fakeOrderRepository),
                updateOrderStatusUseCase = UpdateOrderStatusUseCase(fakeOrderRepository),
                authRepository = fakeAuthRepository,
                cartRepository = fakeCartRepository,
                customerRepository = fakeCustomerRepository,
                pageDraftRepository = fakePageDraftRepository,
                getCategoriesUseCase = GetCategoriesUseCase(fakePageRepository),
                saveCategoryUseCase = SaveCategoryUseCase(fakePageRepository),
                deleteCategoryUseCase = DeleteCategoryUseCase(fakePageRepository),
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPages should update state with list from repository`() =
        runTest {
            val testPage = Page("1", "Teste ViewModel")
            fakePageRepository.savePage(testPage, "token", isEditing = false)

            viewModel.loadPages()
            advanceUntilIdle()

            val pages = viewModel.pages.value
            assertEquals(1, pages.size)
            assertEquals("Teste ViewModel", pages[0].title)
            assertFalse(viewModel.isLoading.value)
        }

    @Test
    fun `saveDraft should persist page in draft repository`() =
        runTest {
            val testPage = Page("draft-1", "Draft Page")

            viewModel.saveDraft(testPage)

            val draft = viewModel.getDraft("draft-1")
            assertEquals("Draft Page", draft?.title)
        }

    @Test
    fun `clearDraft should remove page from draft repository`() =
        runTest {
            val testPage = Page("draft-2", "To be cleared")
            viewModel.saveDraft(testPage)

            viewModel.clearDraft("draft-2")

            val draft = viewModel.getDraft("draft-2")
            assertNull(draft)
        }

    @Test
    fun `savePage should refresh list and clear draft`() =
        runTest {
            val testPage = Page("p1", "Published")
            viewModel.saveDraft(testPage)

            var completeCalled = false
            viewModel.savePage(testPage, isEditing = false) {
                completeCalled = true
            }
            advanceUntilIdle()

            assertTrue(completeCalled)
            assertEquals(1, viewModel.pages.value.size)
            // O PageViewModel.savePage chama pageDraftRepository.clearDraft(page.id)
            assertNull(viewModel.getDraft("p1"))
        }
}
