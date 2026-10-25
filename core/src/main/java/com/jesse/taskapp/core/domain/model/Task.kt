package com.jesse.taskapp.core.domain.model

/**
 * A task stored on this device.
 *
 * @property id unique row id assigned by Room. Screens pass this id and load the task again.
 * @property title required short name.
 * @property description optional longer text. Empty when the user left it blank.
 * @property createdAtMillis epoch millis when the task was first saved. Never changes.
 * @property updatedAtMillis epoch millis of the last successful save. Equals [createdAtMillis] until the first edit.
 */
data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
