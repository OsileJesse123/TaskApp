package com.jesse.taskapp.feature.taskform.domain

import com.jesse.taskapp.FakeTaskRepository
import com.jesse.taskapp.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [CreateTaskUseCase].
 */
class CreateTaskUseCaseTest {
    private val repository = FakeTaskRepository()
    private val clock = FakeTimeProvider(nowMillis = 50L)
    private val useCase = CreateTaskUseCase(repository, clock)

    /**
     * When the title and description are valid, then the stored text is trimmed and both timestamps match the clock.
     */
    @Test
    fun whenInputIsValid_thenTaskIsStoredTrimmedAtTheClockTime() = runTest {
        val result = useCase("  Buy milk  ", "  oat  ")

        val saved = result as TaskSaveResult.Saved
        assertEquals(1L, saved.id)
        assertEquals(
            FakeTaskRepository.CreatedTask("Buy milk", "oat", 50L),
            repository.created.single(),
        )
    }

    /**
     * When the title is blank, then nothing is written and the title is required.
     */
    @Test
    fun whenTitleIsBlank_thenNothingIsWritten() = runTest {
        val result = useCase("   ", "notes")

        val invalid = result as TaskSaveResult.Invalid
        assertEquals(TitleError.Required, invalid.errors.titleError)
        assertTrue(repository.created.isEmpty())
    }

    /**
     * When the title is past the limit, then nothing is written.
     */
    @Test
    fun whenTitleIsTooLong_thenNothingIsWritten() = runTest {
        val result = useCase("a".repeat(TaskInputRules.TITLE_MAX_LENGTH + 1), "")

        val invalid = result as TaskSaveResult.Invalid
        assertEquals(TitleError.TooLong, invalid.errors.titleError)
        assertTrue(repository.created.isEmpty())
    }

    /**
     * When the description is past the limit, then nothing is written.
     */
    @Test
    fun whenDescriptionIsTooLong_thenNothingIsWritten() = runTest {
        val result = useCase("Buy milk", "a".repeat(TaskInputRules.DESCRIPTION_MAX_LENGTH + 1))

        val invalid = result as TaskSaveResult.Invalid
        assertEquals(DescriptionError.TooLong, invalid.errors.descriptionError)
        assertTrue(repository.created.isEmpty())
    }

    /**
     * When the database write throws, then the result is a failure.
     */
    @Test
    fun whenWriteThrows_thenResultIsFailed() = runTest {
        repository.failWrites = true

        val result = useCase("Buy milk", "")

        assertEquals(TaskSaveResult.Failed, result)
    }
}
