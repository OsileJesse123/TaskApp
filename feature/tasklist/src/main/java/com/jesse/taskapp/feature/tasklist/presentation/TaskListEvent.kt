package com.jesse.taskapp.feature.tasklist.presentation

/**
 * One-shot effects for the task list.
 *
 * Load and delete failures are announced once. The steady unavailable body stays on
 * [TaskListUiState] so the explanation is still there after the snackbar and after rotation.
 * Opening a task or the create form is a direct callback: the gesture itself is the one-shot.
 */
sealed interface TaskListEvent {
    /**
     * The task stream failed.
     */
    data object ShowLoadError : TaskListEvent

    /**
     * Bulk delete failed and the tasks are still stored.
     */
    data object ShowDeleteError : TaskListEvent
}
