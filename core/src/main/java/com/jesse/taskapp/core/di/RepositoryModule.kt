package com.jesse.taskapp.core.di

import com.jesse.taskapp.core.data.repository.TaskRepositoryImpl
import com.jesse.taskapp.core.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the Room repository as the app-wide [TaskRepository].
 *
 * The database module provides the DAO. This binding stays installed when instrumentation tests
 * replace the database, so those tests use the same repository pointed at the in-memory database.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /**
     * Binds [TaskRepositoryImpl] to [TaskRepository].
     *
     * @param repository Room-backed repository.
     * @return repository shared by every feature use case.
     */
    @Binds
    @Singleton
    abstract fun bindTaskRepository(repository: TaskRepositoryImpl): TaskRepository
}
