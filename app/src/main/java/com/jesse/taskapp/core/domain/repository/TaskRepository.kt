package com.jesse.taskapp.core.domain.repository

import com.jesse.taskapp.core.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Persistence contract for tasks.
 *
 * The implementation lives in core, not in a feature. The list, detail, and form features all
 * store the same task, and no feature is allowed to depend on another. A repository owned by
 * one feature would force the others to depend on it.
 */
interface TaskRepository {
    /**
     * Observes tasks that match the search text and optional creation-time window.
     *
     * @param query trimmed search text. Blank matches every title and description.
     * @param createdFromMillis inclusive start of the creation window, or null for no lower bound.
     * @param createdToMillis exclusive end of the creation window, or null for no upper bound.
     * @return a cold flow that re-emits the matching tasks, newest creation time first.
     */
    fun observeTasks(
        query: String,
        createdFromMillis: Long?,
        createdToMillis: Long?,
    ): Flow<List<Task>>

    /**
     * Observes one task.
     *
     * @param id task id passed from the previous screen.
     * @return a flow of the task, or null when that id is not stored.
     */
    fun observeTask(id: Long): Flow<Task?>

    /**
     * Inserts a task and sets both timestamps to the same instant.
     *
     * @param title already trimmed title.
     * @param description already trimmed description. May be empty.
     * @param nowMillis instant to store as both created and edited time.
     * @return the new row id.
     */
    suspend fun create(title: String, description: String, nowMillis: Long): Long

    /**
     * Updates the title, description, and edited time of an existing task.
     *
     * @param id task to update.
     * @param title already trimmed title.
     * @param description already trimmed description. May be empty.
     * @param nowMillis instant to store as the edited time. Created time is left unchanged.
     * @return true when a row was updated, false when [id] does not exist.
     */
    suspend fun update(id: Long, title: String, description: String, nowMillis: Long): Boolean

    /**
     * Deletes every task in [ids].
     *
     * @param ids ids to remove. Callers should skip the database when this is empty.
     */
    suspend fun delete(ids: Set<Long>)
}
