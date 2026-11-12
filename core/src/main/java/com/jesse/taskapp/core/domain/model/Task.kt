package com.jesse.taskapp.core.domain.model

import androidx.appfunctions.AppFunctionSerializable

/**
 * A task stored on this device.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class Task(
    /** Unique row id assigned by Room. */
    val id: Long,
    /** Required short name. */
    val title: String,
    /** Optional longer text. Empty when the user left it blank. */
    val description: String,
    /** Epoch millis when the task was first saved. Never changes. */
    val createdAtMillis: Long,
    /** Epoch millis of the last successful save. */
    val updatedAtMillis: Long,
    /** Caller key from an idempotent create; null when the task was saved from the form. */
    val idempotencyKey: String? = null,
    /** Monotonically increasing version, incremented after every successful update. */
    val version: Long = 1L,
)
