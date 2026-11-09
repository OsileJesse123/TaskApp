package com.jesse.taskapp.core.common

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
 * Limits and checks for task title and description before they are written.
 */
object TaskInputRules {
    const val TITLE_MAX_LENGTH: Int = 120
    const val DESCRIPTION_MAX_LENGTH: Int = 2000

    /**
     * Checks a title and description before they are written.
     *
     * @param title raw title from the field or caller.
     * @param description raw description from the field or caller.
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
