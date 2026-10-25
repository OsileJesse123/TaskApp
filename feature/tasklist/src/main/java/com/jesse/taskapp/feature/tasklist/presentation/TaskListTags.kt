package com.jesse.taskapp.feature.tasklist.presentation

/**
 * Test tags and shared ids for the task list.
 */
object TaskListTags {
    const val SEARCH: String = "task_search"
    const val CREATE: String = "task_create"
    const val FILTER: String = "task_filter_date"
    const val CLEAR_FILTER: String = "task_clear_date"
    const val DELETE: String = "task_delete_selected"
    const val CONFIRM_DELETE: String = "task_confirm_delete"

    /**
     * Tag for one row.
     *
     * @param id task id.
     * @return a stable test tag for that row.
     */
    fun row(id: Long): String = "task_row_$id"
}
