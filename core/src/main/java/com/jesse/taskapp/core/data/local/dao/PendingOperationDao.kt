package com.jesse.taskapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jesse.taskapp.core.data.local.entity.PendingOperationEntity
import com.jesse.taskapp.core.domain.model.PendingOperationStatus

@Dao
interface PendingOperationDao {

    @Insert
    suspend fun insert(operation: PendingOperationEntity)

    @Query(
        """
        SELECT * FROM pending_operations
        WHERE id = :id
        LIMIT 1
        """,
    )
    suspend fun findById(id: String): PendingOperationEntity?

    @Query(
        """
        UPDATE pending_operations
        SET status = :newStatus,
            updatedAtMillis = :updatedAtMillis
        WHERE id = :id
        AND status = :expectedStatus
        """,
    )
    suspend fun updateStatus(
        id: String,
        expectedStatus: PendingOperationStatus,
        newStatus: PendingOperationStatus,
        updatedAtMillis: Long,
    ): Int

    @Query(
        """
        SELECT * FROM pending_operations
        WHERE status IN (:statuses)
        ORDER BY requestedAtMillis ASC
        """,
    )
    suspend fun findByStatuses(
        statuses: Set<PendingOperationStatus>,
    ): List<PendingOperationEntity>
}