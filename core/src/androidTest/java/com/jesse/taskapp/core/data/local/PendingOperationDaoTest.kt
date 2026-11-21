package com.jesse.taskapp.core.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jesse.taskapp.core.data.local.dao.PendingOperationDao
import com.jesse.taskapp.core.data.local.entity.PendingOperationEntity
import com.jesse.taskapp.core.domain.model.PendingOperationStatus
import com.jesse.taskapp.core.domain.model.PendingOperationType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingOperationDaoTest {

    private lateinit var database: TaskDatabase
    private lateinit var dao: PendingOperationDao

    @Before
    fun setup(){
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            TaskDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.pendingOperationDao()
    }

    @After
    fun tearDown(){
        database.close()
    }

    @Test
    fun insertAndFindById_returnsStoredOperation() = runTest {
        val operation = PendingOperationEntity(
            id = "operation-1",
            type = PendingOperationType.DELETE_TASK_PERMANENTLY,
            taskId = 5L,
            expectedTaskVersion = 2L,
            taskTitleSnapshot = "Buy groceries",
            status = PendingOperationStatus.AWAITING_USER_APPROVAL,
            requestedAtMillis = 100L,
            expiresAtMillis = 10_000L,
            updatedAtMillis = 100L
        )

        dao.insert(operation)

        val result = dao.findById("operation-1")

        assertEquals(operation, result)
    }

    @Test
    fun updateStatus_withMatchingExpectedStatus_updatesOperation() = runTest {
        val operation = PendingOperationEntity(
            id = "operation-1",
            type = PendingOperationType.DELETE_TASK_PERMANENTLY,
            taskId = 5L,
            expectedTaskVersion = 2L,
            taskTitleSnapshot = "Buy groceries",
            status = PendingOperationStatus.AWAITING_USER_APPROVAL,
            requestedAtMillis = 100L,
            expiresAtMillis = 10_000L,
            updatedAtMillis = 100L
        )

        dao.insert(operation)

        val rowsUpdated = dao.updateStatus(
            id = "operation-1",
            expectedStatus = PendingOperationStatus.AWAITING_USER_APPROVAL,
            newStatus = PendingOperationStatus.AWAITING_BIOMETRIC,
            updatedAtMillis = 200L
        )

        assertEquals(1, rowsUpdated)
        assertEquals(
            PendingOperationStatus.AWAITING_BIOMETRIC,
            dao.findById("operation-1")?.status
        )
    }
}