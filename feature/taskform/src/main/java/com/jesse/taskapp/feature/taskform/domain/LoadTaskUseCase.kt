package com.jesse.taskapp.feature.taskform.domain

import com.jesse.taskapp.core.domain.model.Task
import com.jesse.taskapp.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Reads one task so the edit form can fill its fields.
 *
 * This is separate from the detail feature's observer. The form must not depend on the detail
 * feature, and the form needs a single snapshot rather than a long-lived subscription.
 *
 * @param repository shared task store.
 */
class LoadTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    /**
     * Loads the task with [id].
     *
     * @param id task id from the edit route. Null is not passed here; create mode skips this use case.
     * @return the stored task, or null when the id is missing.
     */
    suspend operator fun invoke(id: Long): Task? {
        // first() takes the current row and cancels the Room subscription.
        return repository.observeTask(id).first()
    }
}
