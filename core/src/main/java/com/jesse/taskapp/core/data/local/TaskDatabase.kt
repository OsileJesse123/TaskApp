package com.jesse.taskapp.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jesse.taskapp.core.data.local.converter.PendingOperationConverters
import com.jesse.taskapp.core.data.local.dao.PendingOperationDao
import com.jesse.taskapp.core.data.local.dao.TaskDao
import com.jesse.taskapp.core.data.local.entity.PendingOperationEntity
import com.jesse.taskapp.core.data.local.entity.TaskEntity

/**
 * Local database that stores tasks.
 */
@TypeConverters(PendingOperationConverters::class)
@Database(
    entities = [TaskEntity::class, PendingOperationEntity::class],
    version = 4,
    exportSchema = true,
)
abstract class TaskDatabase : RoomDatabase() {
    /**
     * Returns the task DAO.
     *
     * @return DAO used by [com.jesse.taskapp.core.data.repository.TaskRepositoryImpl].
     */
    abstract fun taskDao(): TaskDao

    abstract fun pendingOperationDao(): PendingOperationDao
}
