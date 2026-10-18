package com.jesse.taskapp.core.di

import android.content.Context
import androidx.room.Room
import com.jesse.taskapp.core.data.local.TaskDao
import com.jesse.taskapp.core.data.local.TaskDatabase
import com.jesse.taskapp.core.data.repository.TaskRepositoryImpl
import com.jesse.taskapp.core.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Replaces [DatabaseModule] with an in-memory database for instrumentation tests.
 *
 * A file database would keep tasks from the previous test run.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class],
)
object TestDatabaseModule {
    /**
     * Builds a database that exists only for this process.
     *
     * @param context application context from the Hilt test app.
     * @return an in-memory [TaskDatabase].
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TaskDatabase {
        return Room.inMemoryDatabaseBuilder(context, TaskDatabase::class.java).build()
    }

    /**
     * Exposes the DAO from the in-memory database.
     *
     * @param database in-memory database.
     * @return task DAO.
     */
    @Provides
    fun provideTaskDao(database: TaskDatabase): TaskDao = database.taskDao()

    /**
     * Uses the same repository implementation as production, pointed at the in-memory database.
     *
     * @param taskDao in-memory DAO.
     * @return repository injected into feature use cases during the test.
     */
    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository = TaskRepositoryImpl(taskDao)
}
