package com.jesse.taskapp

import com.jesse.taskapp.core.domain.model.Task
import com.jesse.taskapp.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * In-memory [TaskRepository] for unit tests.
 *
 * Filtering here is only so ViewModel tests see a realistic list. The SQL behaviour is covered
 * by the Room test on device.
 */
class FakeTaskRepository : TaskRepository {
    private val tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId: Long = 1

    /**
     * Search strings passed to [observeTasks], in call order.
     */
    val observedQueries: MutableList<String> = mutableListOf()

    /**
     * Inclusive creation bounds passed to [observeTasks].
     */
    val observedFrom: MutableList<Long?> = mutableListOf()

    /**
     * Exclusive creation bounds passed to [observeTasks].
     */
    val observedTo: MutableList<Long?> = mutableListOf()

    /**
     * Id sets passed to [delete].
     */
    val deletedIds: MutableList<Set<Long>> = mutableListOf()

    /**
     * Arguments of successful [create] calls.
     */
    val created: MutableList<CreatedTask> = mutableListOf()

    /**
     * Arguments of [update] calls that found a row.
     */
    val updates: MutableList<UpdatedTask> = mutableListOf()

    /**
     * When true, writes throw [IOException].
     */
    var failWrites: Boolean = false

    /**
     * When true, observe calls throw [IOException].
     */
    var failObserve: Boolean = false

    /**
     * Replaces the stored tasks.
     *
     * @param value tasks that later observations should see.
     */
    fun setTasks(value: List<Task>) {
        tasks.value = value
        nextId = (value.maxOfOrNull(Task::id) ?: 0) + 1
    }

    /**
     * Records the query and returns the tasks that match it.
     *
     * @param query trimmed search text.
     * @param createdFromMillis inclusive bound, or null.
     * @param createdToMillis exclusive bound, or null.
     * @return matching tasks, newest first.
     */
    override fun observeTasks(
        query: String,
        createdFromMillis: Long?,
        createdToMillis: Long?,
    ): Flow<List<Task>> {
        // Throw from inside the flow so ViewModel catch operators see a failed stream, not a
        // constructor failure before a Flow exists.
        observedQueries += query
        observedFrom += createdFromMillis
        observedTo += createdToMillis
        return flow {
            if (failObserve) {
                throw IOException("observe failed")
            }
            emitAll(
                tasks.map { list ->
                    list.filter { task ->
                        val matchesQuery = query.isBlank() ||
                            task.title.contains(query, ignoreCase = true) ||
                            task.description.contains(query, ignoreCase = true)
                        val matchesFrom = createdFromMillis == null || task.createdAtMillis >= createdFromMillis
                        val matchesTo = createdToMillis == null || task.createdAtMillis < createdToMillis
                        matchesQuery && matchesFrom && matchesTo
                    }.sortedWith(compareByDescending<Task> { it.createdAtMillis }.thenByDescending { it.id })
                },
            )
        }
    }

    /**
     * Returns the stored task with [id].
     *
     * @param id task id.
     * @return a flow of that task, or null.
     */
    override fun observeTask(id: Long): Flow<Task?> {
        return flow {
            if (failObserve) {
                throw IOException("observe failed")
            }
            emitAll(tasks.map { list -> list.find { it.id == id } })
        }
    }

    /**
     * Stores a new task.
     *
     * @param title trimmed title.
     * @param description trimmed description.
     * @param nowMillis both timestamps.
     * @return the assigned id.
     */
    override suspend fun create(title: String, description: String, nowMillis: Long): Long {
        if (failWrites) {
            throw IOException("create failed")
        }
        val id = nextId++
        val task = Task(id, title, description, nowMillis, nowMillis)
        tasks.value = tasks.value + task
        created += CreatedTask(title, description, nowMillis)
        return id
    }

    /**
     * Updates a stored task without changing its created time.
     *
     * @param id task to update.
     * @param title new title.
     * @param description new description.
     * @param nowMillis new edited time.
     * @return true when the id existed.
     */
    override suspend fun update(
        id: Long,
        title: String,
        description: String,
        nowMillis: Long,
    ): Boolean {
        if (failWrites) {
            throw IOException("update failed")
        }
        val current = tasks.value
        val existing = current.find { it.id == id } ?: return false
        val updated = existing.copy(
            title = title,
            description = description,
            updatedAtMillis = nowMillis,
        )
        tasks.value = current.map { if (it.id == id) updated else it }
        updates += UpdatedTask(id, title, description, nowMillis)
        return true
    }

    /**
     * Removes [ids] from the store.
     *
     * @param ids ids to remove.
     */
    override suspend fun delete(ids: Set<Long>) {
        if (failWrites) {
            throw IOException("delete failed")
        }
        deletedIds += ids
        tasks.value = tasks.value.filterNot { it.id in ids }
    }

    /**
     * Arguments recorded from [create].
     *
     * @property title stored title.
     * @property description stored description.
     * @property nowMillis timestamp passed by the use case.
     */
    data class CreatedTask(
        val title: String,
        val description: String,
        val nowMillis: Long,
    )

    /**
     * Arguments recorded from [update].
     *
     * @property id updated id.
     * @property title stored title.
     * @property description stored description.
     * @property nowMillis edited timestamp passed by the use case.
     */
    data class UpdatedTask(
        val id: Long,
        val title: String,
        val description: String,
        val nowMillis: Long,
    )
}
