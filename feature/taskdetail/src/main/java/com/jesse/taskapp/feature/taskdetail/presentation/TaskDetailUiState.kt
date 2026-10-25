package com.jesse.taskapp.feature.taskdetail.presentation

/**
 * Rendered state of the task detail screen.
 */
sealed interface TaskDetailUiState {
    /**
     * The row has not been read yet.
     */
    data object Loading : TaskDetailUiState

    /**
     * No row exists for the id passed from the list.
     */
    data object NotFound : TaskDetailUiState

    /**
     * The task stream failed.
     */
    data object LoadFailed : TaskDetailUiState

    /**
     * A stored task.
     *
     * @property title task title.
     * @property description task description. Blank when the user left it empty.
     * @property createdAtLabel formatted creation time.
     * @property updatedAtLabel formatted last-edit time.
     * @property showDeleteConfirmation true while the delete dialog is open.
     */
    data class Content(
        val title: String,
        val description: String,
        val createdAtLabel: String,
        val updatedAtLabel: String,
        val showDeleteConfirmation: Boolean,
    ) : TaskDetailUiState
}
