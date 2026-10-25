package com.jesse.taskapp.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * SQL access for the tasks table.
 */
@Dao
interface TaskDao {
    /**
     * Observes tasks matching a LIKE pattern and an optional creation window.
     *
     * [query] is either empty, which skips the text filter, or a LIKE pattern whose `%`, `_`, and
     * `!` characters were escaped by the repository. The bounds are epoch millis computed in the
     * use case, so this statement does not call SQLite date functions.
     *
     * @param query escaped LIKE pattern, or empty when the user is not searching.
     * @param createdFromMillis inclusive lower bound, or null.
     * @param createdToMillis exclusive upper bound, or null.
     * @return matching rows, newest creation time first, then highest id.
     */
    @Query(
        """
        SELECT * FROM tasks
        WHERE (:query = '' OR title LIKE :query ESCAPE '!' OR description LIKE :query ESCAPE '!')
        AND (:createdFromMillis IS NULL OR createdAtMillis >= :createdFromMillis)
        AND (:createdToMillis IS NULL OR createdAtMillis < :createdToMillis)
        ORDER BY createdAtMillis DESC, id DESC
        """,
    )
    fun observeTasks(
        query: String,
        createdFromMillis: Long?,
        createdToMillis: Long?,
    ): Flow<List<TaskEntity>>

    /**
     * Observes one row.
     *
     * @param id primary key.
     * @return the row, or null when it is missing.
     */
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<TaskEntity?>

    /**
     * Inserts a row.
     *
     * @param task row to insert. Its id is ignored and generated.
     * @return the generated primary key.
     */
    @Insert
    suspend fun insert(task: TaskEntity): Long

    /**
     * Updates the editable columns of one row.
     *
     * @param id row to update.
     * @param title new title.
     * @param description new description.
     * @param updatedAtMillis new edited instant.
     * @return the number of rows changed.
     */
    @Query(
        """
        UPDATE tasks
        SET title = :title, description = :description, updatedAtMillis = :updatedAtMillis
        WHERE id = :id
        """,
    )
    suspend fun update(
        id: Long,
        title: String,
        description: String,
        updatedAtMillis: Long,
    ): Int

    /**
     * Deletes the given ids in one statement.
     *
     * @param ids primary keys to remove.
     */
    @Query("DELETE FROM tasks WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: Set<Long>)
}
