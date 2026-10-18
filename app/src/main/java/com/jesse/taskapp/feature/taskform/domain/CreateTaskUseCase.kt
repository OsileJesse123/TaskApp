package com.jesse.taskapp.feature.taskform.domain

import com.jesse.taskapp.core.common.TimeProvider
import com.jesse.taskapp.core.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Creates a task after the form fields pass validation.
 *
 * @param repository shared task store.
 * @param timeProvider clock used for both timestamps.
 */
class CreateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val timeProvider: TimeProvider,
) {
    /**
     * Validates and inserts a task.
     *
     * @param title raw title.
     * @param description raw description.
     * @return the save result. Invalid input is not written.
     */
    suspend operator fun invoke(title: String, description: String): TaskSaveResult {
        val errors = TaskInputRules.validate(title, description)
        if (errors != null) {
            return TaskSaveResult.Invalid(errors)
        }
        return try {
            // Trim only after validation so the stored text matches what was checked.
            val id = repository.create(
                title = title.trim(),
                description = description.trim(),
                nowMillis = timeProvider.nowMillis(),
            )
            TaskSaveResult.Saved(id)
        } catch (_: Exception) {
            // The message is fixed in the UI so a SQLite exception is not shown to the user.
            TaskSaveResult.Failed
        }
    }
}
