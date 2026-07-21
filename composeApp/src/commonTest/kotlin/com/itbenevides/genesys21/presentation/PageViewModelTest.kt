package com.itbenevides.genesys21.presentation

import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.usecase.*
import com.itbenevides.genesys21.mocks.FakeAuthRepository
import com.itbenevides.genesys21.mocks.FakePageDraftRepository
import com.itbenevides.genesys21.mocks.FakePageRepository
import kotlin.test.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock.System.now

@OptIn(ExperimentalCoroutinesApi::class)
class PageViewModelTest {
    private lateinit var viewModel: PageViewModel
    private lateinit var fakePageRepository: FakePageRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakePageDraftRepository: FakePageDraftRepository
    private lateinit var fakeCartRepository: FakeCartRepository
    private lateinit var fakeCustomerRepository: FakeCustomerRepository
    private lateinit var fakeOrderRepository: FakeOrderRepository
    private lateinit var fakeBookingRepository: FakeBookingRepository
    private lateinit var fakeUserRepository: FakeUserRepository

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
        fakeBookingRepository = FakeBookingRepository()
        fakeUserRepository = FakeUserRepository()

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
                getBookingServicesUseCase = GetBookingServicesUseCase(fakeBookingRepository),
                saveBookingServiceUseCase = SaveBookingServiceUseCase(fakeBookingRepository),
                deleteBookingServiceUseCase = DeleteBookingServiceUseCase(fakeBookingRepository),
                getAppointmentsUseCase = GetAppointmentsUseCase(fakeBookingRepository),
                createAppointmentUseCase = CreateAppointmentUseCase(fakeBookingRepository),
                updateAppointmentUseCase = UpdateAppointmentUseCase(fakeBookingRepository),
                validateBookingSlotUseCase = ValidateBookingSlotUseCase(fakeBookingRepository),
                getAvailabilityUseCase = GetAvailabilityUseCase(fakeBookingRepository),
                saveAvailabilityUseCase = SaveAvailabilityUseCase(fakeBookingRepository),
                getUserProfileUseCase = GetUserProfileUseCase(fakeUserRepository),
                saveUserProfileUseCase = SaveUserProfileUseCase(fakeUserRepository),
                getAllUsersUseCase = GetAllUsersUseCase(fakeUserRepository),
                updateUserRoleUseCase = UpdateUserRoleUseCase(fakeUserRepository),
                updateUserStatusUseCase = UpdateUserStatusUseCase(fakeUserRepository),
                getTemplatesUseCase = GetTemplatesUseCase()
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Page Flows ---

    @Test
    fun `loadPages should update state with list from repository`() =
        runTest {
            val testPage = Page("1", "s1", "Teste ViewModel")
            fakePageRepository.savePage(testPage, "token", isEditing = false)
            fakeAuthRepository.setToken("token")

            viewModel.loadPages()
            advanceUntilIdle()

            val pages = viewModel.pages.value
            assertEquals(1, pages.size)
            assertEquals("Teste ViewModel", pages[0].title)
        }

    @Test
    fun `savePage should call repository and refresh list`() =
        runTest {
            fakeAuthRepository.setToken("valid-token")
            val newPage = Page("p1", "s1", "New Page")

            var success = false
            viewModel.savePage(newPage) { success = true }
            advanceUntilIdle()

            assertTrue(success)
            assertEquals(1, viewModel.pages.value.size)
        }

    @Test
    fun `deletePage should call repository and refresh list`() =
        runTest {
            fakeAuthRepository.setToken("token")
            val testPage = Page("1", "s1", "ToDelete")
            fakePageRepository.savePage(testPage, "token", isEditing = false)

            viewModel.deletePage("1") { }
            advanceUntilIdle()

            assertTrue(viewModel.pages.value.isEmpty())
        }

    // --- Booking Flows ---

    @Test
    fun `loadBookingServices should update services state`() =
        runTest {
            val service = BookingService("s1", "s1", "Cabelo", "Corte", 50.0, 30)
            fakeBookingRepository.saveService(service)

            viewModel.loadBookingServices()
            advanceUntilIdle()

            assertEquals(1, viewModel.services.value.size)
            assertEquals("Cabelo", viewModel.services.value[0].name)
        }

    @Test
    fun `createAppointment should add appointment when slot is valid`() =
        runTest {
            val futureTime = now().plus(kotlin.time.Duration.parse("1h"))
            val appointment =
                Appointment(
                    id = "a1",
                    storeId = "s1",
                    serviceId = "s1",
                    customerName = "Victor",
                    customerPhone = "99999999",
                    startTime = futureTime,
                    endTime = futureTime.plus(kotlin.time.Duration.parse("30m")),
                )

            var success = false
            viewModel.createAppointment("s1", appointment) { success = true }
            advanceUntilIdle()

            assertTrue(success)
            viewModel.loadAppointments(LocalDate(2025, 1, 1), "s1")
            advanceUntilIdle()
            assertEquals(1, viewModel.appointments.value.size)
        }

    @Test
    fun `createAppointment should fail when slot is overlapping`() =
        runTest {
            val futureTime = now().plus(kotlin.time.Duration.parse("2h"))
            val app1 = Appointment(
                id = "1",
                storeId = "s1",
                serviceId = "s1",
                customerName = "C1",
                customerPhone = "1",
                startTime = futureTime,
                endTime = futureTime.plus(kotlin.time.Duration.parse("30m"))
            )
            fakeBookingRepository.createAppointment(app1)

            val app2 = Appointment(
                id = "2",
                storeId = "s1",
                serviceId = "s1",
                customerName = "C2",
                customerPhone = "2",
                startTime = futureTime,
                endTime = futureTime.plus(kotlin.time.Duration.parse("30m"))
            )

            var success = false
            viewModel.createAppointment("s1", app2) { success = true }
            advanceUntilIdle()

            assertFalse(success)
            assertNotNull(viewModel.currentError.value)
            assertEquals("Horário Indisponível", viewModel.currentError.value?.title)
        }

    // --- Cart Flows ---

    @Test
    fun `addToCart should update cart state and total`() =
        runTest {
            val product = Product("pr1", "s1", "Camisa", 100.0)

            viewModel.addToCart(product)
            advanceUntilIdle()

            assertEquals(1, viewModel.cart.value.size)
            assertEquals(100.0, viewModel.cartTotal.value)
            assertEquals(1, viewModel.cartCount.value)
        }

    @Test
    fun `removeFromCart should update cart correctly`() =
        runTest {
            val product = Product("pr1", "s1", "Camisa", 100.0)
            viewModel.addToCart(product)
            advanceUntilIdle()

            viewModel.removeFromCart("pr1")
            advanceUntilIdle()

            assertTrue(viewModel.cart.value.isEmpty())
            assertEquals(0.0, viewModel.cartTotal.value)
        }

    // --- Order Flows ---

    @Test
    fun `submitOrder should clear cart on success`() =
        runTest {
            val product = Product("pr1", "s1", "Camisa", 100.0)
            viewModel.addToCart(product)
            advanceUntilIdle()

            var orderIdResult = ""
            viewModel.submitOrder(null, "PIX") { orderIdResult = it }
            advanceUntilIdle()

            assertNotNull(orderIdResult)
            assertTrue(viewModel.cart.value.isEmpty())
        }

    // --- Category Flows ---

    @Test
    fun `saveCategory should refresh category list`() =
        runTest {
            fakeAuthRepository.setToken("token")
            val cat = Category(id = "c1", storeId = "s1", name = "Test Cat")

            viewModel.saveCategory(cat) { }
            advanceUntilIdle()

            assertEquals(1, viewModel.categories.value.size)
            assertEquals("Test Cat", viewModel.categories.value[0].name)
        }

    // --- Draft Flows ---

    @Test
    fun `saveDraft and getDraft should work together`() =
        runTest {
            val page = Page("d1", "s1", "Draft")
            viewModel.saveDraft(page)

            val loaded = viewModel.getDraft("d1")
            assertEquals("Draft", loaded?.title)
        }

    // --- Image Flows ---

    @Test
    fun `uploadImage should return url on success`() =
        runTest {
            fakeAuthRepository.setToken("token")
            var resultUrl = ""
            viewModel.uploadImage(byteArrayOf(1, 2, 3), "test.jpg") { resultUrl = it }
            advanceUntilIdle()

            assertEquals("https://example.com/test.jpg", resultUrl)
        }

    // --- Auth Flows ---

    @Test
    fun `signOut should clear state`() =
        runTest {
            val testPage = Page("1", "s1", "Page")
            fakePageRepository.savePage(testPage, "token", isEditing = false)
            fakeAuthRepository.setToken("token")
            viewModel.loadPages()
            advanceUntilIdle()

            viewModel.signOut()
            advanceUntilIdle()

            assertTrue(viewModel.pages.value.isEmpty())
        }
}
