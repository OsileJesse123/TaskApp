package com.jesse.taskapp.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Provides coroutine dispatchers and the process-wide scope.
 *
 * Call sites inject a dispatcher instead of reading [Dispatchers] directly, so a test can pass a
 * test dispatcher.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    /**
     * Provides a scope that lives for the whole process.
     *
     * Use it for work that must keep running across screen navigation, such as watching session
     * state or keeping a cache that outlives one screen. [SupervisorJob] keeps one failed child
     * from cancelling the others. The scope runs on the IO dispatcher.
     *
     * @param ioDispatcher IO dispatcher from [provideIoDispatcher].
     * @return a singleton scope that lives until the process ends.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @Dispatcher(TaskAppDispatchers.IO) ioDispatcher: CoroutineDispatcher,
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + ioDispatcher)
    }

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
 * Marks the process-wide [CoroutineScope].
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationScope

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
