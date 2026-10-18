package com.jesse.taskapp.core.di

import android.content.Context
import androidx.room.Room
import com.jesse.taskapp.core.data.local.TaskDao
import com.jesse.taskapp.core.data.local.TaskDatabase
import com.jesse.taskapp.core.data.repository.TaskRepositoryImpl
import com.jesse.taskapp.core.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the on-device task database and the repository that uses it.
 *
 * Instrumentation tests replace this module with an in-memory database so a test does not
 * keep tasks written by the previous run.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /**
     * Builds the file-backed task database.
     *
     * @param context application context, so the database outlives any activity.
     * @return the singleton [TaskDatabase].
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TaskDatabase {
        // One process-wide database. Feature packages receive it only through TaskRepository.
        return Room.databaseBuilder(
            context,
            TaskDatabase::class.java,
            DATABASE_NAME,
        ).build()
    }

    /**
     * Exposes the task DAO from [database].
     *
     * @param database singleton database.
     * @return DAO used by the repository.
     */
    @Provides
    fun provideTaskDao(database: TaskDatabase): TaskDao {
        return database.taskDao()
    }

    /**
     * Binds the Room repository as the app-wide [TaskRepository].
     *
     * @param taskDao task DAO.
     * @return repository shared by every feature use case.
     */
    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return TaskRepositoryImpl(taskDao)
    }

    private const val DATABASE_NAME = "tasks.db"
}
