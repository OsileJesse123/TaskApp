package com.jesse.taskapp.feature.taskform.domain

import com.jesse.taskapp.FakeTaskRepository
import com.jesse.taskapp.FakeTimeProvider
import com.jesse.taskapp.sampleTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [UpdateTaskUseCase].
 */
class UpdateTaskUseCaseTest {
    private val repository = FakeTaskRepository()
    private val clock = FakeTimeProvider(nowMillis = 80L)
    private val useCase = UpdateTaskUseCase(repository, clock)

    /**
     * When the task exists and the fields are valid, then the edited time changes and the created time does not.
     */
    @Test
    fun whenTaskExists_thenEditedTimeChangesAndCreatedTimeDoesNot() = runTest {
        repository.setTasks(listOf(sampleTask(id = 4, createdAtMillis = 10, updatedAtMillis = 10)))

        val result = useCase(4, "  Updated  ", " note ")

        assertEquals(TaskSaveResult.Saved(4), result)
        assertEquals(
            FakeTaskRepository.UpdatedTask(4, "Updated", "note", 80L),
            repository.updates.single(),
        )
        // The fake keeps the original created time. The Room test checks the same rule in SQL.
        val stored = repository.observeTask(4).first()
        assertEquals(10L, stored?.createdAtMillis)
        assertEquals(80L, stored?.updatedAtMillis)
    }

    /**
     * When the title is blank, then the stored task is left unchanged.
     */
    @Test
    fun whenTitleIsBlank_thenTaskIsUnchanged() = runTest {
        repository.setTasks(listOf(sampleTask(id = 4, title = "Keep")))

        val result = useCase(4, " ", "note")

        assertTrue(result is TaskSaveResult.Invalid)
        assertTrue(repository.updates.isEmpty())
    }

    /**
     * When the id is missing, then the result is not found.
     */
    @Test
    fun whenIdIsMissing_thenResultIsNotFound() = runTest {
        val result = useCase(9, "Title", "")

        assertEquals(TaskSaveResult.NotFound, result)
    }

    /**
     * When the write throws, then the result is a failure.
     */
    @Test
    fun whenWriteThrows_thenResultIsFailed() = runTest {
        repository.setTasks(listOf(sampleTask(id = 4)))
        repository.failWrites = true

        val result = useCase(4, "Title", "")

        assertEquals(TaskSaveResult.Failed, result)
    }
}
