package com.jesse.taskapp.feature.taskdetail.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jesse.taskapp.FakeTaskRepository
import com.jesse.taskapp.MainDispatcherRule
import com.jesse.taskapp.core.common.TaskDateFormatter
import com.jesse.taskapp.core.navigation.TASK_ID_KEY
import com.jesse.taskapp.feature.taskdetail.domain.DeleteTaskUseCase
import com.jesse.taskapp.feature.taskdetail.domain.ObserveTaskUseCase
import com.jesse.taskapp.sampleTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [TaskDetailViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTaskRepository()

    /**
     * Builds a detail ViewModel for [id].
     *
     * @param id task id placed in saved state, or null when the argument is missing.
     * @return the ViewModel under test.
     */
    private fun viewModel(id: Long?): TaskDetailViewModel {
        val handle = if (id == null) SavedStateHandle() else SavedStateHandle(mapOf(TASK_ID_KEY to id))
        return TaskDetailViewModel(
            savedStateHandle = handle,
            observeTask = ObserveTaskUseCase(repository),
            deleteTask = DeleteTaskUseCase(repository),
            dateFormatter = TaskDateFormatter(),
        )
    }

    /**
     * When the id is stored, then the screen shows that task.
     */
    @Test
    fun whenTaskExists_thenContentShowsIt() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(id = 3, title = "Buy milk", description = "Oat")))
        val viewModel = viewModel(3)

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem() as TaskDetailUiState.Content
            assertEquals("Buy milk", state.title)
            assertEquals("Oat", state.description)
        }
    }

    /**
     * When the id is missing, then the screen is not-found.
     */
    @Test
    fun whenTaskIsMissing_thenStateIsNotFound() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel(9)

        viewModel.uiState.test {
            advanceUntilIdle()
            assertEquals(TaskDetailUiState.NotFound, expectMostRecentItem())
        }
    }

    /**
     * When delete succeeds, then the route is asked to leave once.
     */
    @Test
    fun whenDeleteSucceeds_thenDeletedEventIsEmittedOnce() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(3)))
        val viewModel = viewModel(3)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onConfirmDelete()
            advanceUntilIdle()
            assertEquals(TaskDetailEvent.TaskDeleted, awaitItem())
        }
        viewModel.events.test {
            expectNoEvents()
        }
        assertEquals(listOf(setOf(3L)), repository.deletedIds)
    }

    /**
     * When delete fails, then the error is not replayed to the next collector.
     */
    @Test
    fun whenDeleteFails_thenErrorIsNotReplayed() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(3)))
        val viewModel = viewModel(3)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        repository.failWrites = true

        viewModel.events.test {
            viewModel.onConfirmDelete()
            advanceUntilIdle()
            assertEquals(TaskDetailEvent.ShowDeleteError, awaitItem())
        }
        viewModel.events.test {
            expectNoEvents()
        }
    }

    /**
     * When a task is on screen and the user asks to delete, then the confirmation dialog is shown.
     */
    @Test
    fun whenDeleteIsRequested_thenConfirmationIsShown() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(3)))
        val viewModel = viewModel(3)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onDeleteClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value as TaskDetailUiState.Content
        assertTrue(state.showDeleteConfirmation)
    }

    /**
     * When the task stream fails, then the screen stays on load-failed and announces it once.
     */
    @Test
    fun whenLoadFails_thenStateIsLoadFailed() = runTest(mainDispatcherRule.dispatcher) {
        repository.failObserve = true
        val viewModel = viewModel(3)
        backgroundScope.launch { viewModel.uiState.collect { } }

        viewModel.events.test {
            advanceUntilIdle()
            assertEquals(TaskDetailEvent.ShowLoadError, awaitItem())
        }
        assertEquals(TaskDetailUiState.LoadFailed, viewModel.uiState.value)
    }
}
