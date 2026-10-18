package com.jesse.taskapp.core.data.repository

import com.jesse.taskapp.core.data.local.TaskDao
import com.jesse.taskapp.core.data.local.TaskEntity
import com.jesse.taskapp.core.data.mapper.toTask
import com.jesse.taskapp.core.domain.model.Task
import com.jesse.taskapp.core.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * [TaskRepository] backed by Room.
 *
 * @param taskDao SQL access to the tasks table.
 */
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
) : TaskRepository {
    /**
     * Observes tasks for a search string and creation window.
     *
     * @param query trimmed search text. Blank matches everything.
     * @param createdFromMillis inclusive creation bound, or null.
     * @param createdToMillis exclusive creation bound, or null.
     * @return domain tasks in the DAO's order.
     */
    override fun observeTasks(
        query: String,
        createdFromMillis: Long?,
        createdToMillis: Long?,
    ): Flow<List<Task>> {
        // Escape LIKE wildcards so a search for "100%" does not match every title that contains "100".
        // '!' is the escape character because a backslash is easy to mangle inside a Room query string.
        val pattern = if (query.isBlank()) "" else likePattern(query)
        return taskDao.observeTasks(pattern, createdFromMillis, createdToMillis)
            .map { rows -> rows.map(TaskEntity::toTask) }
    }

    /**
     * Observes one task by id.
     *
     * @param id task id from navigation.
     * @return the task, or null when the row is gone.
     */
    override fun observeTask(id: Long): Flow<Task?> {
        return taskDao.observeById(id).map { row -> row?.toTask() }
    }

    /**
     * Inserts a task with both timestamps set to [nowMillis].
     *
     * @param title trimmed title.
     * @param description trimmed description.
     * @param nowMillis instant for both timestamps.
     * @return the generated id.
     */
    override suspend fun create(title: String, description: String, nowMillis: Long): Long {
        return withContext(Dispatchers.IO) {
            // Created and edited start equal. A later update changes only the edited time.
            val entity = TaskEntity(
                title = title,
                description = description,
                createdAtMillis = nowMillis,
                updatedAtMillis = nowMillis,
            )
            taskDao.insert(entity)
        }
    }

    /**
     * Updates text and the edited time, leaving the created time as it is.
     *
     * @param id row to update.
     * @param title trimmed title.
     * @param description trimmed description.
     * @param nowMillis new edited instant.
     * @return true when a row changed.
     */
    override suspend fun update(
        id: Long,
        title: String,
        description: String,
        nowMillis: Long,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val rows = taskDao.update(
                id = id,
                title = title,
                description = description,
                updatedAtMillis = nowMillis,
            )
            rows > 0
        }
    }

    /**
     * Deletes the given ids.
     *
     * @param ids primary keys to remove.
     */
    override suspend fun delete(ids: Set<Long>) {
        withContext(Dispatchers.IO) {
            // An empty IN () is invalid SQL. Callers avoid that; this is a second guard.
            if (ids.isNotEmpty()) {
                taskDao.deleteByIds(ids)
            }
        }
    }

    /**
     * Builds a LIKE pattern that treats user `%` and `_` as literal characters.
     *
     * @param query trimmed search text.
     * @return a pattern wrapped in `%` for a contains search.
     */
    private fun likePattern(query: String): String {
        // Escape the escape character first so a typed "!" stays literal.
        val escaped = query
            .replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_")
        return "%$escaped%"
    }
}
