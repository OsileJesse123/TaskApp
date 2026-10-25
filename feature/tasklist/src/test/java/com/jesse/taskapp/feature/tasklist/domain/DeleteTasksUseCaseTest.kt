package com.jesse.taskapp.feature.tasklist.domain

import com.jesse.taskapp.FakeTaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [DeleteTasksUseCase].
 */
class DeleteTasksUseCaseTest {
    private val repository = FakeTaskRepository()
    private val useCase = DeleteTasksUseCase(repository)

    /**
     * When the selection is empty, then the repository is not asked to delete.
     */
    @Test
    fun whenSelectionIsEmpty_thenRepositoryIsNotCalled() = runTest {
        useCase(emptySet())

        assertTrue(repository.deletedIds.isEmpty())
    }

    /**
     * When ids are confirmed, then those ids are deleted together.
     */
    @Test
    fun whenIdsAreConfirmed_thenTheyAreDeletedTogether() = runTest {
        useCase(setOf(2L, 5L))

        assertEquals(listOf(setOf(2L, 5L)), repository.deletedIds)
    }
}
