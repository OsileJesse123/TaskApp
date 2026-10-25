package com.jesse.taskapp.feature.tasklist.presentation

import app.cash.turbine.test
import com.jesse.taskapp.FakeTaskRepository
import com.jesse.taskapp.MainDispatcherRule
import com.jesse.taskapp.core.common.TaskDateFormatter
import com.jesse.taskapp.feature.tasklist.domain.DeleteTasksUseCase
import com.jesse.taskapp.feature.tasklist.domain.ObserveTasksUseCase
import com.jesse.taskapp.feature.tasklist.domain.creationDateRangeWindow
import com.jesse.taskapp.sampleTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Tests for [TaskListViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTaskRepository()
    private lateinit var viewModel: TaskListViewModel

    /**
     * Creates the ViewModel after [com.jesse.taskapp.MainDispatcherRule] has installed the test dispatcher.
     */
    @Before
    fun setUp() {
        viewModel = TaskListViewModel(
            observeTasks = ObserveTasksUseCase(repository),
            deleteTasks = DeleteTasksUseCase(repository),
            dateFormatter = TaskDateFormatter(),
        )
    }

    /**
     * When tasks are stored and no filter is active, then the list shows them.
     */
    @Test
    fun whenTasksExist_thenContentShowsThem() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(id = 1, title = "Buy milk")))

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            val tasks = state.content as TaskListContent.Tasks
            assertEquals("Buy milk", tasks.tasks.single().title)
        }
    }

    /**
     * When nothing is stored, then the body is empty rather than no-results.
     */
    @Test
    fun whenNothingIsStored_thenBodyIsEmpty() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.uiState.test {
            advanceUntilIdle()
            assertEquals(TaskListContent.Empty, expectMostRecentItem().content)
        }
    }

    /**
     * When the search matches nothing, then the body is no-results after the debounce.
     */
    @Test
    fun whenSearchMatchesNothing_thenBodyIsNoResults() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(id = 1, title = "Buy milk")))
        // Keep the state flow subscribed. WhileSubscribed stops the query when nobody is collecting.
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onQueryChange("zzz")
        advanceTimeBy(300)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.content is TaskListContent.NoResults)
        assertEquals("zzz", viewModel.uiState.value.query)
    }

    /**
     * When the user types, then the search field text updates before the debounce queries the database.
     */
    @Test
    fun whenUserTypes_thenQueryInUiStateUpdatesBeforeDebounce() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(id = 1, title = "Buy milk")))
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        repository.observedQueries.clear()

        viewModel.onQueryChange("l")
        runCurrent()

        assertEquals("l", viewModel.uiState.value.query)
        val tasks = viewModel.uiState.value.content as TaskListContent.Tasks
        assertEquals("Buy milk", tasks.tasks.single().title)
        assertFalse(repository.observedQueries.contains("l"))

        viewModel.onQueryChange("lh")
        runCurrent()

        assertEquals("lh", viewModel.uiState.value.query)
        assertFalse(repository.observedQueries.contains("lh"))
    }

    /**
     * When the user types quickly, then only the settled search is sent to the repository.
     */
    @Test
    fun whenUserTypesQuickly_thenOnlyTheSettledSearchIsQueried() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        repository.observedQueries.clear()

        viewModel.onQueryChange("a")
        viewModel.onQueryChange("ab")
        // runCurrent executes work that is already due. advanceUntilIdle would also jump the
        // 300ms debounce and hide the fact that the in-between text was not queried.
        runCurrent()
        assertFalse(repository.observedQueries.contains("a"))
        assertFalse(repository.observedQueries.contains("ab"))

        advanceTimeBy(300)
        runCurrent()
        assertTrue(repository.observedQueries.contains("ab"))
        assertFalse(repository.observedQueries.contains("a"))
    }

    /**
     * When a creation range is selected, then the repository receives that local window.
     */
    @Test
    fun whenCreationRangeIsSelected_thenRepositoryReceivesThatWindow() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        val start = LocalDate.of(2026, 10, 17).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 10, 19).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        viewModel.onCreationRangeSelected(start, end)
        advanceUntilIdle()

        val expected = creationDateRangeWindow(start, end, ZoneId.systemDefault())
        Assert.assertEquals(expected.startMillisInclusive, repository.observedFrom.last())
        Assert.assertEquals(expected.endMillisExclusive, repository.observedTo.last())
        assertEquals(start, viewModel.uiState.value.createdFromUtcMillis)
        assertEquals(end, viewModel.uiState.value.createdToUtcMillis)
        assertEquals(
            TaskDateFormatter().formatPickerDateRange(start, end),
            viewModel.uiState.value.createdOnLabel,
        )
    }

    /**
     * When the user long-presses and then taps, then the selected count follows the selection.
     */
    @Test
    fun whenUserSelectsRows_thenSelectedCountFollowsTheSelection() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(1), sampleTask(2)))
        viewModel.uiState.test {
            advanceUntilIdle()
            viewModel.onStartSelection(1)
            advanceUntilIdle()
            val selected = expectMostRecentItem()
            assertEquals(1, selected.selectedCount)
            assertTrue(selected.selectionMode)

            viewModel.onToggleSelected(1)
            advanceUntilIdle()
            val cleared = expectMostRecentItem()
            assertEquals(0, cleared.selectedCount)
            assertFalse(cleared.selectionMode)
        }
    }

    /**
     * When delete is confirmed, then the selected ids are removed and selection ends.
     */
    @Test
    fun whenDeleteIsConfirmed_thenSelectedIdsAreRemoved() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(1, title = "Buy milk"), sampleTask(2)))
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        viewModel.onStartSelection(1)
        viewModel.onConfirmDelete()
        viewModel.onConfirmDelete()
        advanceUntilIdle()

        assertEquals(listOf(setOf(1L)), repository.deletedIds)
        assertFalse(viewModel.uiState.value.selectionMode)
        val titles = (viewModel.uiState.value.content as TaskListContent.Tasks).tasks.map { it.title }
        assertEquals(listOf("Task 2"), titles)
    }

    /**
     * When delete fails, then the error is emitted once and is not replayed to a new collector.
     */
    @Test
    fun whenDeleteFails_thenErrorIsEmittedOnce() = runTest(mainDispatcherRule.dispatcher) {
        repository.setTasks(listOf(sampleTask(1)))
        repository.failWrites = true
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        viewModel.onStartSelection(1)

        viewModel.events.test {
            viewModel.onConfirmDelete()
            advanceUntilIdle()
            assertEquals(TaskListEvent.ShowDeleteError, awaitItem())
        }
        viewModel.events.test {
            expectNoEvents()
        }
        assertTrue(viewModel.uiState.value.selectionMode)
    }

    /**
     * When the task stream fails, then the body stays unavailable and a load error is emitted.
     */
    @Test
    fun whenTaskStreamFails_thenBodyIsUnavailable() = runTest(mainDispatcherRule.dispatcher) {
        repository.failObserve = true
        // WhileSubscribed does not open the database stream until the screen collects state.
        backgroundScope.launch { viewModel.uiState.collect { } }

        viewModel.events.test {
            advanceUntilIdle()
            assertEquals(TaskListEvent.ShowLoadError, awaitItem())
        }
        assertEquals(TaskListContent.Unavailable, viewModel.uiState.value.content)
    }
}
