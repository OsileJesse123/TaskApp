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
     * Observes tasks matching [query] and, when both bounds are set, the creation-date range.
     *
     * @param query raw search field text. Blank and surrounding spaces are ignored.
     * @param createdFromUtcMillis UTC midnight of the first selected day, or null when no range is set.
     * @param createdToUtcMillis UTC midnight of the last selected day, or null when no range is set.
     * @param zone zone used to turn picker days into a millis window.
     * @return matching tasks, newest first.
     */
    operator fun invoke(
        query: String,
        createdFromUtcMillis: Long?,
        createdToUtcMillis: Long?,
        zone: ZoneId,
    ): Flow<List<Task>> {
        // Trim so " milk " searches for "milk". An empty result means no text filter.
        val normalized = query.trim()
        val window = if (createdFromUtcMillis != null && createdToUtcMillis != null) {
            creationDateRangeWindow(createdFromUtcMillis, createdToUtcMillis, zone)
        } else {
            null
        }
        return repository.observeTasks(
            query = normalized,
            createdFromMillis = window?.startMillisInclusive,
            createdToMillis = window?.endMillisExclusive,
        )
    }
}
