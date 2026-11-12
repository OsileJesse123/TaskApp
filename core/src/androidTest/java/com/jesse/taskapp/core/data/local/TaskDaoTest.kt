package com.jesse.taskapp.core.data.local

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * Room-backed tests for [TaskDao] version and optimistic delete behavior.
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var database: TaskDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TaskDatabase::class.java).build()
        dao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    /**
     * When a task is inserted, then its version is one.
     */
    @Test
    fun whenTaskIsInserted_thenVersionIsOne() = runTest {
        val id = dao.insert(
            TaskEntity(
                title = "Buy milk",
                description = "",
                createdAtMillis = 100,
                updatedAtMillis = 100,
            ),
        )

        val row = dao.observeById(id).first()

        assertEquals(1L, row?.version)
    }

    /**
     * When a task is updated, then its version increases by one.
     */
    @Test
    fun whenTaskIsUpdated_thenVersionIncreases() = runTest {
        val id = dao.insert(
            TaskEntity(
                title = "Buy milk",
                description = "",
                createdAtMillis = 100,
                updatedAtMillis = 100,
            ),
        )

        val changed = dao.update(id, "Buy bread", "Loaf", 250)
        val row = dao.observeById(id).first()

        assertEquals(1, changed)
        assertEquals(2L, row?.version)
    }

    /**
     * When delete matches the current version, then the row is removed and one is returned.
     */
    @Test
    fun whenDeleteIfVersionMatchesCurrentVersion_thenRowIsRemoved() = runTest {
        val id = dao.insert(
            TaskEntity(
                title = "Buy milk",
                description = "",
                createdAtMillis = 100,
                updatedAtMillis = 100,
            ),
        )

        val deleted = dao.deleteIfVersionMatches(id, expectedVersion = 1)

        assertEquals(1, deleted)
        assertNull(dao.observeById(id).first())
    }

    /**
     * When delete uses a stale version, then nothing is removed and zero is returned.
     */
    @Test
    fun whenDeleteIfVersionMatchesStaleVersion_thenRowRemains() = runTest {
        val id = dao.insert(
            TaskEntity(
                title = "Buy milk",
                description = "",
                createdAtMillis = 100,
                updatedAtMillis = 100,
            ),
        )
        dao.update(id, "Buy bread", "Loaf", 250)

        val deleted = dao.deleteIfVersionMatches(id, expectedVersion = 1)

        assertEquals(0, deleted)
        assertEquals(2L, dao.observeById(id).first()?.version)
    }
}
