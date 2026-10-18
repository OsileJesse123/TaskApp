package com.jesse.taskapp.feature.tasklist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesse.taskapp.core.common.TaskDateFormatter
import com.jesse.taskapp.core.domain.model.Task
import com.jesse.taskapp.feature.tasklist.domain.DeleteTasksUseCase
import com.jesse.taskapp.feature.tasklist.domain.ObserveTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

/**
 * State and one-shot events for the task list.
 *
 * @param observeTasks loads tasks for the debounced search and the creation day.
 * @param deleteTasks deletes the confirmed selection.
 * @param dateFormatter formats creation times and the selected filter day.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val observeTasks: ObserveTasksUseCase,
    private val deleteTasks: DeleteTasksUseCase,
    private val dateFormatter: TaskDateFormatter,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val createdOnUtcMillis = MutableStateFlow<Long?>(null)
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val showDeleteConfirmation = MutableStateFlow(false)

    // No replay: a snackbar must not show again after rotation. One slot lets a failure emitted
    // just before collection still reach the route.
    private val _events = MutableSharedFlow<TaskListEvent>(extraBufferCapacity = 1)

    /**
     * Failures that the route shows once in a snackbar.
     */
    val events: SharedFlow<TaskListEvent> = _events.asSharedFlow()

    private var deleteInFlight: Boolean = false

    /**
     * Latest list state. Collection stops five seconds after the screen leaves composition.
     */
    val uiState: StateFlow<TaskListUiState> = combine(
        query,
        query.debounce { value -> if (value.isBlank()) 0L else SEARCH_DEBOUNCE_MILLIS },
        createdOnUtcMillis,
        selectedIds,
        showDeleteConfirmation,
    ) { typed, debounced, createdOn, selected, confirm ->
        ListInputs(typed, debounced, createdOn, selected, confirm)
    }.flatMapLatest { inputs ->
        observeTasks(inputs.debouncedQuery, inputs.createdOnUtcMillis, ZoneId.systemDefault())
            .map { tasks -> inputs.toState(tasks) }
            .catch {
                // Keep a steady unavailable body. The snackbar is only the first announcement.
                _events.emit(TaskListEvent.ShowLoadError)
                emit(inputs.toState(tasks = null))
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = TaskListUiState(),
    )

    /**
     * Updates the search field and leaves selection so a hidden row cannot be deleted unseen.
     *
     * @param value new search text.
     */
    fun onQueryChange(value: String) {
        query.value = value
        clearSelection()
    }

    /**
     * Filters the list to the creation day chosen in the date picker.
     *
     * @param utcMillis UTC midnight reported by the Material date picker.
     */
    fun onCreationDateSelected(utcMillis: Long) {
        createdOnUtcMillis.value = utcMillis
        clearSelection()
    }

    /**
     * Removes the creation-day filter.
     */
    fun onCreationDateCleared() {
        createdOnUtcMillis.value = null
        clearSelection()
    }

    /**
     * Starts selection with [id], or adds it when selection is already active.
     *
     * @param id row the user long-pressed.
     */
    fun onStartSelection(id: Long) {
        selectedIds.update { current -> current + id }
    }

    /**
     * Adds or removes [id] from the selection.
     *
     * @param id row the user tapped while selecting.
     */
    fun onToggleSelected(id: Long) {
        selectedIds.update { current ->
            if (id in current) current - id else current + id
        }
    }

    /**
     * Leaves selection mode and closes the confirm dialog.
     */
    fun onCancelSelection() {
        clearSelection()
    }

    /**
     * Opens the bulk-delete confirmation when something is selected.
     */
    fun onDeleteSelection() {
        if (selectedIds.value.isEmpty() || deleteInFlight) {
            return
        }
        showDeleteConfirmation.value = true
    }

    /**
     * Closes the confirmation dialog without deleting.
     */
    fun onDismissDelete() {
        showDeleteConfirmation.value = false
    }

    /**
     * Deletes the current selection. A second call while the first is running is ignored.
     */
    fun onConfirmDelete() {
        if (deleteInFlight) {
            return
        }
        val ids = selectedIds.value
        if (ids.isEmpty()) {
            showDeleteConfirmation.value = false
            return
        }
        deleteInFlight = true
        viewModelScope.launch {
            try {
                deleteTasks(ids)
                clearSelection()
            } catch (_: Exception) {
                showDeleteConfirmation.value = false
                _events.emit(TaskListEvent.ShowDeleteError)
            } finally {
                deleteInFlight = false
            }
        }
    }

    /**
     * Clears selection and the dialog together.
     */
    private fun clearSelection() {
        selectedIds.value = emptySet()
        showDeleteConfirmation.value = false
    }

    /**
     * Maps the latest inputs and rows into render state.
     *
     * @param tasks matching tasks, or null when the stream failed.
     * @return the state the screen should render.
     */
    private fun ListInputs.toState(tasks: List<Task>?): TaskListUiState {
        val label = createdOnUtcMillis?.let(dateFormatter::formatPickerDate)
        if (tasks == null) {
            return TaskListUiState(
                query = typedQuery,
                createdOnUtcMillis = createdOnUtcMillis,
                createdOnLabel = label,
                content = TaskListContent.Unavailable,
            )
        }
        val visibleIds = tasks.map(Task::id).toSet()
        val visibleSelected = selectedIds.intersect(visibleIds)
        val items = tasks.map { task ->
            TaskListItemUi(
                id = task.id,
                title = task.title,
                description = task.description,
                createdAtLabel = dateFormatter.formatDateTime(task.createdAtMillis),
                selected = task.id in visibleSelected,
            )
        }
        val content = when {
            tasks.isEmpty() && debouncedQuery.isBlank() && createdOnUtcMillis == null ->
                TaskListContent.Empty
            tasks.isEmpty() -> TaskListContent.NoResults
            else -> TaskListContent.Tasks(items)
        }
        return TaskListUiState(
            query = typedQuery,
            createdOnUtcMillis = createdOnUtcMillis,
            createdOnLabel = label,
            selectionMode = visibleSelected.isNotEmpty(),
            selectedCount = visibleSelected.size,
            showDeleteConfirmation = showDeleteConfirmation && visibleSelected.isNotEmpty(),
            content = content,
        )
    }

    /**
     * Values that define one list query and the chrome around it.
     *
     * @property typedQuery search field text before debounce.
     * @property debouncedQuery search text sent to the database.
     * @property createdOnUtcMillis selected creation day, or null.
     * @property selectedIds ids the user has selected.
     * @property showDeleteConfirmation whether the confirm dialog was requested.
     */
    private data class ListInputs(
        val typedQuery: String,
        val debouncedQuery: String,
        val createdOnUtcMillis: Long?,
        val selectedIds: Set<Long>,
        val showDeleteConfirmation: Boolean,
    )

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS: Long = 300
        const val STOP_TIMEOUT_MILLIS: Long = 5_000
    }
}
