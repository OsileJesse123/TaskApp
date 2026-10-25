package com.jesse.taskapp.core.di

import com.jesse.taskapp.core.common.SystemTimeProvider
import com.jesse.taskapp.core.common.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the device clock as [TimeProvider].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {
    /**
     * Binds [SystemTimeProvider] to [TimeProvider].
     *
     * @param provider device clock.
     * @return the clock injected into write use cases.
     */
    @Binds
    @Singleton
    abstract fun bindTimeProvider(provider: SystemTimeProvider): TimeProvider
}
