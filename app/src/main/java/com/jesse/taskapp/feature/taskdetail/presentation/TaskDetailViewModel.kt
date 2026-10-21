package com.jesse.taskapp.feature.taskdetail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesse.taskapp.core.common.TaskDateFormatter
import com.jesse.taskapp.core.domain.model.Task
import com.jesse.taskapp.core.navigation.TASK_ID_KEY
import com.jesse.taskapp.feature.taskdetail.domain.DeleteTaskUseCase
import com.jesse.taskapp.feature.taskdetail.domain.ObserveTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State and one-shot events for one task.
 *
 * The id comes from navigation state, not from a passed task object.
 *
 * @param savedStateHandle navigation arguments. The detail route stores [TASK_ID_KEY].
 * @param observeTask loads the row for that id.
 * @param deleteTask deletes the row after confirmation.
 * @param dateFormatter formats the created and edited times.
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTask: ObserveTaskUseCase,
    private val deleteTask: DeleteTaskUseCase,
    private val dateFormatter: TaskDateFormatter,
) : ViewModel() {
    private val taskId: Long = savedStateHandle.get<Long>(TASK_ID_KEY) ?: MISSING_TASK_ID
    private val showDeleteConfirmation = MutableStateFlow(false)
    private var deleteInFlight: Boolean = false

    // No replay, so a deleted navigation or a snackbar does not run again after rotation.
    private val _events = MutableSharedFlow<TaskDetailEvent>(extraBufferCapacity = 1)

    /**
     * Delete failure and successful deletion. The route collects this once.
     */
    val events: SharedFlow<TaskDetailEvent> = _events.asSharedFlow()

    /**
     * Latest detail state.
     */
    val uiState: StateFlow<TaskDetailUiState> = combine(
        observeTask(taskId),
        showDeleteConfirmation,
    ) { task, confirm ->
        task.toState(confirm)
    }.catch {
        _events.emit(TaskDetailEvent.ShowLoadError)
        emit(TaskDetailUiState.LoadFailed)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = TaskDetailUiState.Loading,
    )

    /**
     * Opens the delete dialog when a task is on screen.
     */
    fun onDeleteClick() {
        if (deleteInFlight || uiState.value !is TaskDetailUiState.Content) {
            return
        }
        showDeleteConfirmation.value = true
    }

    /**
     * Closes the delete dialog.
     */
    fun onDismissDelete() {
        showDeleteConfirmation.value = false
    }

    /**
     * Deletes the task and asks the route to return to the list.
     */
    fun onConfirmDelete() {
        if (deleteInFlight || taskId == MISSING_TASK_ID) {
            return
        }
        deleteInFlight = true
        viewModelScope.launch {
            try {
                deleteTask(taskId)
                showDeleteConfirmation.value = false
                _events.emit(TaskDetailEvent.TaskDeleted)
            } catch (_: Exception) {
                showDeleteConfirmation.value = false
                _events.emit(TaskDetailEvent.ShowDeleteError)
            } finally {
                deleteInFlight = false
            }
        }
    }

    /**
     * Maps a stored task to detail content.
     *
     * @param confirm whether the delete dialog should be open.
     * @return not-found when the row is missing, otherwise the formatted content.
     */
    private fun Task?.toState(confirm: Boolean): TaskDetailUiState {
        if (this == null) {
            return TaskDetailUiState.NotFound
        }
        return TaskDetailUiState.Content(
            title = title,
            description = description,
            createdAtLabel = dateFormatter.formatDateTime(createdAtMillis),
            updatedAtLabel = dateFormatter.formatDateTime(updatedAtMillis),
            showDeleteConfirmation = confirm,
        )
    }

    private companion object {
        const val MISSING_TASK_ID: Long = -1L
        const val STOP_TIMEOUT_MILLIS: Long = 5_000
    }
}
