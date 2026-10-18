package com.jesse.taskapp.feature.taskdetail.domain

import com.jesse.taskapp.core.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Deletes the task shown on the detail screen.
 *
 * @param repository shared task store.
 */
class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    /**
     * Deletes [id].
     *
     * @param id task shown on the detail screen.
     */
    suspend operator fun invoke(id: Long) {
        // One id is still a set so list and detail share the same repository delete.
        repository.delete(setOf(id))
    }
}
