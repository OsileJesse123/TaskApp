package com.jesse.taskapp.feature.tasklist.domain

import com.jesse.taskapp.core.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Deletes a set of tasks in one call.
 *
 * @param repository shared task store.
 */
class DeleteTasksUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    /**
     * Deletes [ids].
     *
     * @param ids tasks the user confirmed. Empty does nothing, because SQL `IN ()` is invalid.
     */
    suspend operator fun invoke(ids: Set<Long>) {
        // Skip the database when selection was cleared before confirm ran.
        if (ids.isEmpty()) {
            return
        }
        repository.delete(ids)
    }
}
