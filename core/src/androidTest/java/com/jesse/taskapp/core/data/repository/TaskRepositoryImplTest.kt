package com.jesse.taskapp.core.data.repository

import android.content.Context
import androidx.room.Room
import com.jesse.taskapp.core.data.local.TaskDatabase
import com.jesse.taskapp.core.domain.model.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * Room-backed tests for [TaskRepositoryImpl].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TaskRepositoryImplTest {
    private lateinit var database: TaskDatabase
    private lateinit var repository: TaskRepositoryImpl

    /**
     * Opens a fresh in-memory database for one test.
     */
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TaskDatabase::class.java).build()
        // A test dispatcher stands in for the injected IO dispatcher, so these tests do not use Dispatchers.IO.
        repository = TaskRepositoryImpl(database.taskDao(), UnconfinedTestDispatcher())
    }

    /**
     * Closes the database so the next test does not see its rows.
     */
    @After
    fun tearDown() {
        database.close()
    }

    /**
     * When a task is created, then observing it returns the same text and timestamps.
     */
    @Test
    fun whenTaskIsCreated_thenObserveReturnsIt() = runTest {
        val id = repository.create("Buy milk", "Oat", nowMillis = 100)

        val task = repository.observeTask(id).first()

        Assert.assertEquals(Task(id, "Buy milk", "Oat", 100, 100), task)
    }

    /**
     * When a task is updated, then the created time stays and the edited time changes.
     */
    @Test
    fun whenTaskIsUpdated_thenCreatedTimeStays() = runTest {
        val id = repository.create("Buy milk", "", nowMillis = 100)

        val updated = repository.update(id, "Buy bread", "Loaf", nowMillis = 250)
        val task = repository.observeTask(id).first()

        Assert.assertTrue(updated)
        Assert.assertEquals(100L, task?.createdAtMillis)
        Assert.assertEquals(250L, task?.updatedAtMillis)
        Assert.assertEquals("Buy bread", task?.title)
    }

    /**
     * When the search text is part of the title in a different case, then that task is returned.
     */
    @Test
    fun whenSearchMatchesTitleIgnoringCase_thenTaskIsReturned() = runTest {
        repository.create("Buy milk", "", nowMillis = 100)
        repository.create("Write notes", "", nowMillis = 200)

        val tasks = repository.observeTasks("MILK", null, null).first()

        Assert.assertEquals(listOf("Buy milk"), tasks.map(Task::title))
    }

    /**
     * When the search contains a percent sign, then it matches that character and not a wildcard.
     */
    @Test
    fun whenSearchContainsPercent_thenPercentIsLiteral() = runTest {
        repository.create("100% done", "", nowMillis = 200)
        repository.create("100X done", "", nowMillis = 100)

        val tasks = repository.observeTasks("100%", null, null).first()

        Assert.assertEquals(listOf("100% done"), tasks.map(Task::title))
    }

    /**
     * When a creation window is set, then only tasks inside that window are returned.
     */
    @Test
    fun whenCreationWindowIsSet_thenOnlyTasksInsideItAreReturned() = runTest {
        repository.create("Inside", "", nowMillis = 150)
        repository.create("Before", "", nowMillis = 50)
        repository.create("At end", "", nowMillis = 200)

        val tasks =
            repository.observeTasks("", createdFromMillis = 100, createdToMillis = 200).first()

        Assert.assertEquals(listOf("Inside"), tasks.map(Task::title))
    }

    /**
     * When several ids are deleted, then those rows are gone and the others remain.
     */
    @Test
    fun whenIdsAreDeleted_thenOnlyThoseRowsAreRemoved() = runTest {
        val first = repository.create("One", "", nowMillis = 100)
        val second = repository.create("Two", "", nowMillis = 200)
        val third = repository.create("Three", "", nowMillis = 300)

        repository.delete(setOf(first, third))
        val remaining = repository.observeTasks("", null, null).first()

        Assert.assertEquals(listOf(second), remaining.map(Task::id))
    }

    /**
     * When the id is not stored, then observe emits null.
     */
    @Test
    fun whenIdIsMissing_thenObserveEmitsNull() = runTest {
        val task = repository.observeTask(44).first()

        Assert.assertNull(task)
    }

    /**
     * When delete is called with no ids, then the stored tasks stay.
     */
    @Test
    fun whenDeleteIsEmpty_thenTasksStay() = runTest {
        val id = repository.create("Keep", "", nowMillis = 100)

        repository.delete(emptySet())
        val task = repository.observeTask(id).first()

        Assert.assertEquals("Keep", task?.title)
    }
}