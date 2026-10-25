package com.jesse.taskapp.feature.taskdetail.presentation

/**
 * One-shot effects for task detail.
 *
 * Not-found and load-failed stay on [TaskDetailUiState] so they can be previewed and so they
 * remain after rotation. This channel is for a failure or navigation that must happen once.
 */
sealed interface TaskDetailEvent {
    /**
     * The task could not be loaded.
     */
    data object ShowLoadError : TaskDetailEvent

    /**
     * Delete failed and the task is still stored.
     */
    data object ShowDeleteError : TaskDetailEvent

    /**
     * The task was deleted. The route returns to the list.
     */
    data object TaskDeleted : TaskDetailEvent
}
