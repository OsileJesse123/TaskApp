package com.jesse.taskapp.feature.taskform.domain

import com.jesse.taskapp.core.common.TimeProvider
import com.jesse.taskapp.core.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Updates a task after the form fields pass validation.
 *
 * @param repository shared task store.
 * @param timeProvider clock used for the edited timestamp.
 */
class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val timeProvider: TimeProvider,
) {
    /**
     * Validates and updates the task with [id].
     *
     * @param id task being edited.
     * @param title raw title.
     * @param description raw description.
     * @return the save result. Created time is not changed.
     */
    suspend operator fun invoke(id: Long, title: String, description: String): TaskSaveResult {
        val errors = TaskInputRules.validate(title, description)
        if (errors != null) {
            return TaskSaveResult.Invalid(errors)
        }
        return try {
            val updated = repository.update(
                id = id,
                title = title.trim(),
                description = description.trim(),
                nowMillis = timeProvider.nowMillis(),
            )
            if (updated) {
                TaskSaveResult.Saved(id)
            } else {
                TaskSaveResult.NotFound
            }
        } catch (_: Exception) {
            TaskSaveResult.Failed
        }
    }
}
