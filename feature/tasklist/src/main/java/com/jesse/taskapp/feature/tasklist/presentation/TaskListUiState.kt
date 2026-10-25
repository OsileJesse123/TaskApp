package com.jesse.taskapp.feature.tasklist.presentation

/**
 * One row on the task list.
 *
 * @property id task id used for navigation and selection.
 * @property title task title.
 * @property description task description. Blank hides the second line.
 * @property createdAtLabel formatted creation time.
 * @property selected whether this row is part of the current bulk selection.
 */
data class TaskListItemUi(
    val id: Long,
    val title: String,
    val description: String,
    val createdAtLabel: String,
    val selected: Boolean,
)

/**
 * What the list body is showing under the search and filter controls.
 */
sealed interface TaskListContent {
    /**
     * The first database emission has not arrived.
     */
    data object Loading : TaskListContent

    /**
     * There are no tasks, and the user is not searching or filtering.
     */
    data object Empty : TaskListContent

    /**
     * Tasks exist, but none match the search or creation date.
     */
    data object NoResults : TaskListContent

    /**
     * The database stream failed.
     */
    data object Unavailable : TaskListContent

    /**
     * Tasks to render.
     *
     * @property tasks visible rows.
     */
    data class Tasks(val tasks: List<TaskListItemUi>) : TaskListContent
}

/**
 * Rendered state of the task list.
 *
 * Empty, no-results, and unavailable stay here so a preview can show them and so they survive
 * rotation. Snackbar failures are [TaskListEvent]s, which are not replayed.
 *
 * @property query text currently in the search field, including characters not yet debounced.
 * @property createdOnUtcMillis UTC midnight of the selected creation day, or null.
 * @property createdOnLabel formatted creation day, or null when no day is selected.
 * @property selectionMode true when at least one visible row is selected.
 * @property selectedCount number of visible selected rows.
 * @property showDeleteConfirmation true while the bulk-delete dialog is open.
 * @property content body under the search and filter controls.
 */
data class TaskListUiState(
    val query: String = "",
    val createdOnUtcMillis: Long? = null,
    val createdOnLabel: String? = null,
    val selectionMode: Boolean = false,
    val selectedCount: Int = 0,
    val showDeleteConfirmation: Boolean = false,
    val content: TaskListContent = TaskListContent.Loading,
)
