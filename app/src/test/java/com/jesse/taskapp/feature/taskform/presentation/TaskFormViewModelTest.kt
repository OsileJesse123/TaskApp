package com.jesse.taskapp.feature.taskform.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jesse.taskapp.FakeTaskRepository
import com.jesse.taskapp.core.navigation.TASK_ID_KEY
import com.jesse.taskapp.FakeTimeProvider
import com.jesse.taskapp.MainDispatcherRule
import com.jesse.taskapp.feature.taskform.domain.CreateTaskUseCase
import com.jesse.taskapp.feature.taskform.domain.LoadTaskUseCase
import com.jesse.taskapp.feature.taskform.domain.TitleError
import com.jesse.taskapp.feature.taskform.domain.UpdateTaskUseCase
import com.jesse.taskapp.sampleTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [TaskFormViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskFormViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTaskRepository()
    private val clock = FakeTimeProvider(nowMillis = 40L)

    /**
     * Builds a form ViewModel. A null id is create mode.
     *
     * @param id edit id, or null to create.
     * @return the ViewModel under test.
     */
    private fun viewModel(id: Long?): TaskFormViewModel {
        val handle = if (id == null) SavedStateHandle() else SavedStateHandle(mapOf(TASK_ID_KEY to id))
        return TaskFormViewModel(
            savedStateHandle = handle,
            loadTask = LoadTaskUseCase(repository),
            createTask = CreateTaskUseCase(repository, clock),
            updateTask = UpdateTaskUseCase(repository, clock),
        )
    }

    /**
     * When a new task is saved, then the saved event is emitted and the row is stored.
     */
    @Test
    fun whenCreateIsSaved_thenSavedEventIsEmitted() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel(null)
        viewModel.onTitleChange("  Buy milk ")
        viewModel.onDescriptionChange(" oat ")

        viewModel.events.test {
            viewModel.onSave()
            viewModel.onSave()
            advanceUntilIdle()
            assertEquals(TaskFormEvent.TaskSaved, awaitItem())
        }
        assertEquals(1, repository.created.size)
        assertEquals("Buy milk", repository.created.single().title)
    }

    /**
     * When the title is blank, then the field error stays on state and nothing is written.
     */
    @Test
    fun whenTitleIsBlank_thenFieldErrorIsShown() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel(null)
        viewModel.onTitleChange("   ")

        viewModel.onSave()
        advanceUntilIdle()

        assertEquals(TitleError.Required, viewModel.uiState.value.titleError)
        assertTrue(repository.created.isEmpty())
        viewModel.events.test {
            expectNoEvents()
        }
    }

    /**
     * When edit mode loads a task, then the fields are filled.
     */
    @Test
    fun whenEditLoadsATask_thenFieldsAreFilled() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(id = 6, title = "Buy milk", description = "Oat")))
        val viewModel = viewModel(6)

        advanceUntilIdle()

        assertEquals("Buy milk", viewModel.uiState.value.title)
        assertEquals("Oat", viewModel.uiState.value.description)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isEdit)
    }

    /**
     * When the edit id is missing, then the form is not-found.
     */
    @Test
    fun whenEditIdIsMissing_thenFormIsNotFound() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel(6)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.notFound)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    /**
     * When the task disappears before save, then the form reports not-found and a save error once.
     */
    @Test
    fun whenUpdateMissesTheRow_thenSaveErrorIsNotReplayed() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(id = 6, title = "Buy milk")))
        val viewModel = viewModel(6)
        advanceUntilIdle()
        repository.setTasks(emptyList())

        viewModel.events.test {
            viewModel.onSave()
            advanceUntilIdle()
            assertEquals(TaskFormEvent.ShowSaveError, awaitItem())
        }
        viewModel.events.test {
            expectNoEvents()
        }
        assertTrue(viewModel.uiState.value.notFound)
    }

    /**
     * When the write throws, then the save error is emitted once.
     */
    @Test
    fun whenSaveThrows_thenErrorIsNotReplayed() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel(null)
        viewModel.onTitleChange("Buy milk")
        repository.failWrites = true

        viewModel.events.test {
            viewModel.onSave()
            advanceUntilIdle()
            assertEquals(TaskFormEvent.ShowSaveError, awaitItem())
        }
        viewModel.events.test {
            expectNoEvents()
        }
    }

    /**
     * When loading the edit task throws, then the form shows load-failed and announces it once.
     */
    @Test
    fun whenLoadThrows_thenLoadFailedIsShown() = runTest(mainDispatcherRule.dispatcher) {
        repository.failObserve = true
        val viewModel = viewModel(6)

        viewModel.events.test {
            advanceUntilIdle()
            assertEquals(TaskFormEvent.ShowLoadError, awaitItem())
        }
        assertTrue(viewModel.uiState.value.loadFailed)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
