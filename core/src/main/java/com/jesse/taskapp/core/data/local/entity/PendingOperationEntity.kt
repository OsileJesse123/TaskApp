package com.jesse.taskapp.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jesse.taskapp.core.domain.model.PendingOperationStatus
import com.jesse.taskapp.core.domain.model.PendingOperationType


@Entity(
    tableName = "pending_operations",
    indices = [
        Index(value = ["status"]),
        Index(value = ["taskId"]),
    ],
)
data class PendingOperationEntity(
    @PrimaryKey
    val id: String,
    val type: PendingOperationType,
    val taskId: Long,
    val expectedTaskVersion: Long,
    val taskTitleSnapshot: String,
    val status: PendingOperationStatus,
    val requestedAtMillis: Long,
    val expiresAtMillis: Long,
    val consentGrantedAtMillis: Long? = null,
    val completedAtMillis: Long? = null,
    val updatedAtMillis: Long,
)
