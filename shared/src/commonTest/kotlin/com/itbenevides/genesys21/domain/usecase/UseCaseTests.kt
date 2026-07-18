package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.mocks.FakePageRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class UseCaseTests {
    private lateinit var repository: FakePageRepository
    private lateinit var getPagesUseCase: GetPagesUseCase
    private lateinit var savePageUseCase: SavePageUseCase

    @BeforeTest
    fun setup() {
        repository = FakePageRepository()
        getPagesUseCase = GetPagesUseCase(repository)
        savePageUseCase = SavePageUseCase(repository)
    }

    @Test
    fun getPagesUseCase_should_return_list_from_repository() =
        runTest {
            val testPage = Page("1", "s1", "Teste")
            repository.savePage(testPage, "token", isEditing = false)

            val result = getPagesUseCase("token")

            assertEquals(1, result.size)
            assertEquals("Teste", result[0].title)
        }

    @Test
    fun savePageUseCase_should_call_save_when_isEditing_is_false() =
        runTest {
            val page = Page("new", "s1", "Nova Página")
            val result = savePageUseCase(page, "token", isEditing = false)

            assertTrue(result.isSuccess)
            assertEquals(1, getPagesUseCase("token").size)
        }

    @Test
    fun savePageUseCase_should_call_update_when_isEditing_is_true() =
        runTest {
            val page = Page("1", "s1", "Original")
            repository.savePage(page, "token", isEditing = false)

            val updatedPage = page.copy(title = "Editada")
            val result = savePageUseCase(updatedPage, "token", isEditing = true)

            assertTrue(result.isSuccess)
            assertEquals("Editada", getPagesUseCase("token")[0].title)
        }

    @Test
    fun savePageUseCase_should_return_failure_when_repository_fails() =
        runTest {
            repository.shouldReturnError = true
            val page = Page("1", "s1", "Erro")

            val result = savePageUseCase(page, "token", isEditing = false)

            assertTrue(result.isFailure)
        }
}
