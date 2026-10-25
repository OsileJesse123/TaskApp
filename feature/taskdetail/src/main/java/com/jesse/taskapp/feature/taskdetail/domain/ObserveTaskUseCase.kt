package com.jesse.taskapp.feature.taskdetail.domain

import com.jesse.taskapp.core.domain.model.Task
import com.jesse.taskapp.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Loads one task for the detail screen.
 *
 * The screen only receives an id. This use case reads the row from local storage.
 *
 * @param repository shared task store.
 */
class ObserveTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    /**
     * Observes the task with [id].
     *
     * @param id id passed from the list.
     * @return the task, or null when it has been deleted.
     */
    operator fun invoke(id: Long): Flow<Task?> {
        return repository.observeTask(id)
    }
}
