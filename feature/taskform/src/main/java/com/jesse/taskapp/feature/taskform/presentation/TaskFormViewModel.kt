package com.jesse.taskapp.feature.taskform.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesse.taskapp.core.navigation.TASK_ID_KEY
import com.jesse.taskapp.feature.taskform.domain.CreateTaskUseCase
import com.jesse.taskapp.feature.taskform.domain.LoadTaskUseCase
import com.jesse.taskapp.feature.taskform.domain.TaskSaveResult
import com.jesse.taskapp.feature.taskform.domain.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State and one-shot events for creating or editing a task.
 *
 * Create has no [TASK_ID_KEY] argument. Edit reads [TASK_ID_KEY] from navigation and loads the row.
 *
 * @param savedStateHandle navigation arguments for the current form destination.
 * @param loadTask reads an existing task for edit mode.
 * @param createTask validates and inserts.
 * @param updateTask validates and updates.
 */
@HiltViewModel
class TaskFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadTask: LoadTaskUseCase,
    private val createTask: CreateTaskUseCase,
    private val updateTask: UpdateTaskUseCase,
) : ViewModel() {
    // Absent on the create route. Present on the edit route under TASK_ID_KEY.
    private val taskId: Long? = savedStateHandle.get<Long>(TASK_ID_KEY)

    private val _uiState = MutableStateFlow(
        TaskFormUiState(
            isEdit = taskId != null,
            isLoading = taskId != null,
        ),
    )

    /**
     * Latest form fields and field errors.
     */
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TaskFormEvent>(extraBufferCapacity = 1)

    /**
     * Save and load failures, plus a successful save. Not replayed.
     */
    val events: SharedFlow<TaskFormEvent> = _events.asSharedFlow()

    private var saveInFlight: Boolean = false

    init {
        val id = taskId
        if (id != null) {
            viewModelScope.launch {
                loadExisting(id)
            }
        }
    }

    /**
     * Replaces the title and clears a stale title error.
     *
     * @param value new title field text.
     */
    fun onTitleChange(value: String) {
        _uiState.update { current -> current.copy(title = value, titleError = null) }
    }

    /**
     * Replaces the description and clears a stale description error.
     *
     * @param value new description field text.
     */
    fun onDescriptionChange(value: String) {
        _uiState.update { current -> current.copy(description = value, descriptionError = null) }
    }

    /**
     * Validates and writes the form. A second call while the first is running is ignored.
     */
    fun onSave() {
        if (saveInFlight || _uiState.value.isLoading || _uiState.value.notFound || _uiState.value.loadFailed) {
            return
        }
        val current = _uiState.value
        saveInFlight = true
        viewModelScope.launch {
            try {
                val result = if (taskId == null) {
                    createTask(current.title, current.description)
                } else {
                    updateTask(taskId, current.title, current.description)
                }
                apply(result)
            } finally {
                saveInFlight = false
            }
        }
    }

    /**
     * Fills the form from storage.
     *
     * @param id task being edited.
     */
    private suspend fun loadExisting(id: Long) {
        try {
            val task = loadTask(id)
            if (task == null) {
                _uiState.update { it.copy(isLoading = false, notFound = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        title = task.title,
                        description = task.description,
                    )
                }
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false, loadFailed = true) }
            _events.emit(TaskFormEvent.ShowLoadError)
        }
    }

    /**
     * Applies a save result to state or to the one-shot channel.
     *
     * @param result outcome from the create or update use case.
     */
    private suspend fun apply(result: TaskSaveResult) {
        when (result) {
            is TaskSaveResult.Saved -> _events.emit(TaskFormEvent.TaskSaved)
            is TaskSaveResult.Invalid -> _uiState.update {
                it.copy(
                    titleError = result.errors.titleError,
                    descriptionError = result.errors.descriptionError,
                )
            }
            TaskSaveResult.NotFound -> {
                _uiState.update { it.copy(notFound = true) }
                _events.emit(TaskFormEvent.ShowSaveError)
            }
            TaskSaveResult.Failed -> _events.emit(TaskFormEvent.ShowSaveError)
        }
    }
}
