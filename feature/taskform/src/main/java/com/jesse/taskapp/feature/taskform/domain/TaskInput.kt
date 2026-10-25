package com.jesse.taskapp.feature.taskform.domain

/**
 * Why a title cannot be saved.
 */
sealed interface TitleError {
    /**
     * The trimmed title is empty.
     */
    data object Required : TitleError

    /**
     * The trimmed title is longer than [TaskInputRules.TITLE_MAX_LENGTH].
     */
    data object TooLong : TitleError
}

/**
 * Why a description cannot be saved.
 */
sealed interface DescriptionError {
    /**
     * The trimmed description is longer than [TaskInputRules.DESCRIPTION_MAX_LENGTH].
     */
    data object TooLong : DescriptionError
}

/**
 * Limits and checks for the task form.
 */
object TaskInputRules {
    const val TITLE_MAX_LENGTH: Int = 120
    const val DESCRIPTION_MAX_LENGTH: Int = 2000

    /**
     * Checks a title and description before they are written.
     *
     * @param title raw title from the field.
     * @param description raw description from the field.
     * @return field errors, or null when both values can be saved.
     */
    fun validate(title: String, description: String): TaskFieldErrors? {
        // Trim before measuring so leading spaces do not count as a title or toward the limit.
        val trimmedTitle = title.trim()
        val trimmedDescription = description.trim()
        val titleError = when {
            trimmedTitle.isEmpty() -> TitleError.Required
            trimmedTitle.length > TITLE_MAX_LENGTH -> TitleError.TooLong
            else -> null
        }
        val descriptionError = if (trimmedDescription.length > DESCRIPTION_MAX_LENGTH) {
            DescriptionError.TooLong
        } else {
            null
        }
        // Null means both fields can be saved. The caller must not write when this returns errors.
        if (titleError == null && descriptionError == null) {
            return null
        }
        return TaskFieldErrors(titleError, descriptionError)
    }
}

/**
 * Field errors found by [TaskInputRules.validate].
 *
 * @property titleError title problem, or null when the title is valid.
 * @property descriptionError description problem, or null when the description is valid.
 */
data class TaskFieldErrors(
    val titleError: TitleError?,
    val descriptionError: DescriptionError?,
)

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
