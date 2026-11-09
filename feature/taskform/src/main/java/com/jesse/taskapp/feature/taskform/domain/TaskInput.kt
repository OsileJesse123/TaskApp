package com.jesse.taskapp.feature.taskform.domain

import com.jesse.taskapp.core.common.TaskFieldErrors

/**
 * Result of creating or updating a task.
 */
sealed interface TaskSaveResult {
    /**
     * The task was written.
     *
     * @property id saved task id.
     */
    data class Saved(val id: Long) : TaskSaveResult

    /**
     * The fields failed validation and nothing was written.
     *
     * @property errors field errors to show on the form.
     */
    data class Invalid(val errors: TaskFieldErrors) : TaskSaveResult

    /**
     * The id passed to an update is no longer stored.
     */
    data object NotFound : TaskSaveResult

    /**
     * The database write threw.
     */
    data object Failed : TaskSaveResult
}
