package com.jesse.taskapp.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

/**
 * Provides coroutine dispatchers.
 *
 * Call sites inject a dispatcher instead of reading [Dispatchers] directly, so a test can pass a
 * test dispatcher.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    /**
     * Provides the IO dispatcher for disk and database work.
     *
     * @return [Dispatchers.IO].
     */
    @Provides
    @Dispatcher(TaskAppDispatchers.IO)
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Provides the default dispatcher for CPU work.
     *
     * @return [Dispatchers.Default].
     */
    @Provides
    @Dispatcher(TaskAppDispatchers.DEFAULT)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

/**
 * Selects which [TaskAppDispatchers] entry a [CoroutineDispatcher] parameter uses.
 *
 * @property dispatcher which dispatcher to inject.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: TaskAppDispatchers)

/**
 * Dispatchers this app injects.
 */
enum class TaskAppDispatchers {
    IO,
    DEFAULT,
}
