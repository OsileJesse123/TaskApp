package com.jesse.taskapp.feature.tasklist.domain

import com.jesse.taskapp.core.domain.model.Task
import com.jesse.taskapp.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId
import javax.inject.Inject

/**
 * Loads the task list for the current search text and creation-date filter.
 *
 * @param repository shared task store.
 */
class ObserveTasksUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    /**
     * Observes tasks matching [query] and, when set, the creation day of [createdOnUtcMillis].
     *
     * @param query raw search field text. Blank and surrounding spaces are ignored.
     * @param createdOnUtcMillis UTC midnight from the date picker, or null when no day is selected.
     * @param zone zone used to turn the picker day into a millis window.
     * @return matching tasks, newest first.
     */
    operator fun invoke(
        query: String,
        createdOnUtcMillis: Long?,
        zone: ZoneId,
    ): Flow<List<Task>> {
        // Trim so " milk " searches for "milk". An empty result means no text filter.
        val normalized = query.trim()
        val window = createdOnUtcMillis?.let { creationDayWindow(it, zone) }
        return repository.observeTasks(
            query = normalized,
            createdFromMillis = window?.startMillisInclusive,
            createdToMillis = window?.endMillisExclusive,
        )
    }
}
