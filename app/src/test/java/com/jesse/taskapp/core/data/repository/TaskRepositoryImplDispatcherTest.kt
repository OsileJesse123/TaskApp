package com.jesse.taskapp.core.data.repository

import com.jesse.taskapp.core.data.local.TaskDao
import com.jesse.taskapp.core.data.local.TaskEntity
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Checks that [TaskRepositoryImpl] runs writes on the dispatcher it is given.
 */
class TaskRepositoryImplDispatcherTest {
    /**
     * When a task is created, then the insert runs on the injected dispatcher.
     */
    @Test
    fun whenCreateIsCalled_thenInsertRunsOnTheInjectedDispatcher() = runTest {
        val dao = FakeTaskDao()
        val dispatcher = RecordingDispatcher()
        val repository = TaskRepositoryImpl(dao, dispatcher)

        val id = repository.create("Buy milk", "Oat", 100)

        assertEquals(1, dispatcher.dispatchCount)
        assertEquals(1L, id)
        assertEquals("Buy milk", dao.inserted.single().title)
        assertEquals("Oat", dao.inserted.single().description)
        assertEquals(100L, dao.inserted.single().createdAtMillis)
        assertEquals(100L, dao.inserted.single().updatedAtMillis)
    }

    /**
     * When a task is updated, then the update runs on the injected dispatcher.
     */
    @Test
    fun whenUpdateIsCalled_thenUpdateRunsOnTheInjectedDispatcher() = runTest {
        val dao = FakeTaskDao(updatedRows = 1)
        val dispatcher = RecordingDispatcher()
        val repository = TaskRepositoryImpl(dao, dispatcher)

        val updated = repository.update(4, "Buy bread", "Loaf", 250)

        assertEquals(1, dispatcher.dispatchCount)
        assertTrue(updated)
        assertEquals(4L, dao.updates.single().id)
        assertEquals("Buy bread", dao.updates.single().title)
        assertEquals("Loaf", dao.updates.single().description)
        assertEquals(250L, dao.updates.single().updatedAtMillis)
    }

    /**
     * When delete is given ids, then the DAO call runs on the injected dispatcher.
     */
    @Test
    fun whenIdsAreDeleted_thenDeleteRunsOnTheInjectedDispatcher() = runTest {
        val dao = FakeTaskDao()
        val dispatcher = RecordingDispatcher()
        val repository = TaskRepositoryImpl(dao, dispatcher)

        repository.delete(setOf(1L, 3L))

        assertEquals(1, dispatcher.dispatchCount)
        assertEquals(setOf(1L, 3L), dao.deletedIds.single())
    }

    /**
     * When delete is given no ids, then the call still uses the dispatcher and the DAO is not called.
     */
    @Test
    fun whenDeleteIsEmpty_thenDaoIsNotCalled() = runTest {
        val dao = FakeTaskDao()
        val dispatcher = RecordingDispatcher()
        val repository = TaskRepositoryImpl(dao, dispatcher)

        repository.delete(emptySet())

        assertEquals(1, dispatcher.dispatchCount)
        assertTrue(dao.deletedIds.isEmpty())
    }

    /**
     * When the insert fails, then create throws that failure.
     */
    @Test
    fun whenInsertThrows_thenCreateThrowsThatFailure() = runTest {
        val dao = FakeTaskDao(insertError = IllegalStateException("disk"))
        val dispatcher = RecordingDispatcher()
        val repository = TaskRepositoryImpl(dao, dispatcher)

        try {
            repository.create("Buy milk", "", 100)
            fail("create should throw when the insert fails")
        } catch (error: IllegalStateException) {
            assertEquals("disk", error.message)
        }
        assertEquals(1, dispatcher.dispatchCount)
        assertTrue(dao.inserted.isEmpty())
    }

    /**
     * Runs each dispatched block immediately and counts how many times it was asked to do so.
     */
    private class RecordingDispatcher : CoroutineDispatcher() {
        var dispatchCount: Int = 0

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            dispatchCount += 1
            block.run()
        }
    }

    private class FakeTaskDao(
        private val updatedRows: Int = 0,
        private val insertError: Throwable? = null,
    ) : TaskDao {
        val inserted = mutableListOf<TaskEntity>()
        val updates = mutableListOf<Update>()
        val deletedIds = mutableListOf<Set<Long>>()

        override fun observeTasks(
            query: String,
            createdFromMillis: Long?,
            createdToMillis: Long?,
        ): Flow<List<TaskEntity>> = emptyFlow()

        override fun observeById(id: Long): Flow<TaskEntity?> = emptyFlow()

        override suspend fun insert(task: TaskEntity): Long {
            insertError?.let { throw it }
            inserted += task
            return inserted.size.toLong()
        }

        override suspend fun update(
            id: Long,
            title: String,
            description: String,
            updatedAtMillis: Long,
        ): Int {
            updates += Update(id, title, description, updatedAtMillis)
            return updatedRows
        }

        override suspend fun deleteByIds(ids: Set<Long>) {
            deletedIds += ids
        }

        data class Update(
            val id: Long,
            val title: String,
            val description: String,
            val updatedAtMillis: Long,
        )
    }
}
