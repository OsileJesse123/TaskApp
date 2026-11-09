package com.jesse.taskapp.core.service

import android.os.CancellationSignal
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import com.jesse.taskapp.FakeTaskRepository
import com.jesse.taskapp.FakeTimeProvider
import com.jesse.taskapp.core.common.TimeProvider
import com.jesse.taskapp.core.domain.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.function.Consumer

/**
 * Tests for [BaseTaskAppFunctionService.createTask].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BaseTaskAppFunctionServiceCreateTaskTest {
    private val repository = FakeTaskRepository()
    private val timeProvider = FakeTimeProvider(nowMillis = 900L)
    private val service = TestTaskAppFunctionService()

    init {
        service.bind(
            repository = repository,
            timeProvider = timeProvider,
            dispatcher = UnconfinedTestDispatcher(),
        )
    }

    /**
     * When input is valid, then createTask returns the stored task with the idempotency key.
     */
    @Test
    fun whenInputIsValid_thenCreateTaskReturnsStoredTask() = runTest {
        val task = service.createTask(
            title = "Buy milk",
            description = "Oat",
            idempotencyKey = "agent-key-1",
        )

        assertEquals(1L, task.id)
        assertEquals("Buy milk", task.title)
        assertEquals("Oat", task.description)
        assertEquals(900L, task.createdAtMillis)
        assertEquals("agent-key-1", task.idempotencyKey)
        assertEquals(1, repository.created.size)
    }

    /**
     * When the same idempotency key is retried, then createTask returns the first task unchanged.
     */
    @Test
    fun whenSameIdempotencyKeyIsRetried_thenReturnsFirstTask() = runTest {
        val first = service.createTask(
            title = "First",
            description = "One",
            idempotencyKey = "retry-key",
        )
        val second = service.createTask(
            title = "Second",
            description = "Two",
            idempotencyKey = "retry-key",
        )

        assertEquals(first.id, second.id)
        assertEquals("First", second.title)
        assertEquals("One", second.description)
        assertEquals(1, repository.created.size)
    }

    /**
     * When a retry uses an invalid title but an existing idempotency key, then the first task is returned.
     */
    @Test
    fun whenRetryHasInvalidTitleButExistingKey_thenReturnsFirstTask() = runTest {
        val first = service.createTask(
            title = "Prepare for interview",
            description = "Notes",
            idempotencyKey = "abc",
        )
        val second = service.createTask(
            title = "   ",
            idempotencyKey = "abc",
        )

        assertEquals(first.id, second.id)
        assertEquals("Prepare for interview", second.title)
        assertEquals(1, repository.created.size)
    }

    private class TestTaskAppFunctionService : BaseTaskAppFunctionService() {
        fun bind(
            repository: TaskRepository,
            timeProvider: TimeProvider,
            dispatcher: kotlinx.coroutines.CoroutineDispatcher,
        ) {
            taskRepository = repository
            this.timeProvider = timeProvider
            defaultDispatcher = dispatcher
        }

        override fun onExecuteFunction(
            request: ExecuteAppFunctionRequest,
            cancellationSignal: CancellationSignal,
            callback: Consumer<ExecuteAppFunctionResponse>,
        ) {
            throw UnsupportedOperationException("not used in unit tests")
        }
    }
}
