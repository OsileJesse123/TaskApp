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
 * @param observeTasks loads tasks for the debounced search and the creation-date range.
 * @param deleteTasks deletes the confirmed selection.
 * @param dateFormatter formats creation times and the selected filter range.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val observeTasks: ObserveTasksUseCase,
    private val deleteTasks: DeleteTasksUseCase,
    private val dateFormatter: TaskDateFormatter,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val dateRange = MutableStateFlow(DateRangeFilter())
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
     * Database results for the settled search. Kept off the typed [query] so the search field can
     * update on each keystroke without waiting for Room.
     */
    private val observedList = combine(
        query.debounce { value -> if (value.isBlank()) 0L else SEARCH_DEBOUNCE_MILLIS },
        dateRange,
    ) { debounced, range ->
        ObservedListInputs(debounced, range)
    }.flatMapLatest { inputs ->
        observeTasks(
            inputs.debouncedQuery,
            inputs.dateRange.fromUtcMillis,
            inputs.dateRange.toUtcMillis,
            ZoneId.systemDefault(),
        )
            .map { tasks -> inputs.toObservedList(tasks) }
            .catch {
                _events.emit(TaskListEvent.ShowLoadError)
                emit(inputs.toObservedList(tasks = null))
            }
    }

    /**
     * Latest list state. Collection stops five seconds after the screen leaves composition.
     *
     * Typed search is combined here directly so [TaskListUiState.query] matches the field as the
     * user types. The body still follows [observedList].
     */
    val uiState: StateFlow<TaskListUiState> = combine(
        query,
        dateRange,
        selectedIds,
        showDeleteConfirmation,
        observedList,
    ) { typed, range, selected, confirm, observed ->
        toState(
            typedQuery = typed,
            dateRange = range,
            selectedIds = selected,
            showDeleteConfirmation = confirm,
            observed = observed,
        )
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
     * Filters the list to tasks created on a day in the chosen range, inclusive of both ends.
     *
     * @param fromUtcMillis UTC midnight of the first selected day.
     * @param toUtcMillis UTC midnight of the last selected day.
     */
    fun onCreationRangeSelected(fromUtcMillis: Long, toUtcMillis: Long) {
        dateRange.value = DateRangeFilter(fromUtcMillis, toUtcMillis)
        clearSelection()
    }

    /**
     * Removes the creation-date range filter.
     */
    fun onCreationDateCleared() {
        dateRange.value = DateRangeFilter()
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
     * @param typedQuery search field text, including characters not yet queried.
     * @param dateRange selected creation-date range, or empty.
     * @param selectedIds ids the user has selected.
     * @param showDeleteConfirmation whether the confirm dialog was requested.
     * @param observed latest database result for the settled search.
     * @return the state the screen should render.
     */
    private fun toState(
        typedQuery: String,
        dateRange: DateRangeFilter,
        selectedIds: Set<Long>,
        showDeleteConfirmation: Boolean,
        observed: ObservedList,
    ): TaskListUiState {
        val label = dateRange.label()
        val tasks = observed.tasks ?: return TaskListUiState(
            query = typedQuery,
            createdFromUtcMillis = dateRange.fromUtcMillis,
            createdToUtcMillis = dateRange.toUtcMillis,
            createdOnLabel = label,
            content = TaskListContent.Unavailable,
        )
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
            tasks.isEmpty() && observed.debouncedQuery.isBlank() && dateRange.isEmpty() ->
                TaskListContent.Empty
            tasks.isEmpty() -> TaskListContent.NoResults
            else -> TaskListContent.Tasks(items)
        }
        return TaskListUiState(
            query = typedQuery,
            createdFromUtcMillis = dateRange.fromUtcMillis,
            createdToUtcMillis = dateRange.toUtcMillis,
            createdOnLabel = label,
            selectionMode = visibleSelected.isNotEmpty(),
            selectedCount = visibleSelected.size,
            showDeleteConfirmation = showDeleteConfirmation && visibleSelected.isNotEmpty(),
            content = content,
        )
    }

    /**
     * @return formatted range for the filter row, or null when no range is set.
     */
    private fun DateRangeFilter.label(): String? {
        val from = fromUtcMillis
        val to = toUtcMillis
        if (from == null || to == null) {
            return null
        }
        return dateFormatter.formatPickerDateRange(from, to)
    }

    /**
     * Search and range that produced one [ObservedList].
     *
     * @property debouncedQuery search text sent to the database.
     * @property dateRange selected creation-date range.
     */
    private data class ObservedListInputs(
        val debouncedQuery: String,
        val dateRange: DateRangeFilter,
    )

    /**
     * UTC picker days chosen for the creation filter.
     *
     * @property fromUtcMillis first day, or null.
     * @property toUtcMillis last day, or null.
     */
    private data class DateRangeFilter(
        val fromUtcMillis: Long? = null,
        val toUtcMillis: Long? = null,
    ) {
        /**
         * @return true when no range is applied.
         */
        fun isEmpty(): Boolean = fromUtcMillis == null && toUtcMillis == null
    }

    /**
     * @param tasks matching tasks, or null when the stream failed.
     * @return database result for these inputs.
     */
    private fun ObservedListInputs.toObservedList(tasks: List<Task>?): ObservedList {
        return ObservedList(
            debouncedQuery = debouncedQuery,
            tasks = tasks,
        )
    }

    /**
     * Latest rows for the settled search.
     *
     * @property debouncedQuery search text that produced [tasks].
     * @property tasks matching tasks, or null when the stream failed.
     */
    private data class ObservedList(
        val debouncedQuery: String,
        val tasks: List<Task>?,
    )

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS: Long = 300
        const val STOP_TIMEOUT_MILLIS: Long = 5_000
    }
}
