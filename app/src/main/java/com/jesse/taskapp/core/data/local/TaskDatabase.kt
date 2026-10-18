package com.jesse.taskapp.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Local database that stores tasks.
 */
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class TaskDatabase : RoomDatabase() {
    /**
     * Returns the task DAO.
     *
     * @return DAO used by [com.jesse.taskapp.core.data.repository.TaskRepositoryImpl].
     */
    abstract fun taskDao(): TaskDao
}
