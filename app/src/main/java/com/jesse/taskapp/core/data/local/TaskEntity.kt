package com.jesse.taskapp.core.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room row for a task.
 *
 * The creation-time index supports the list filter. Domain code never sees this type.
 *
 * @property id auto-generated primary key. Zero only before insert.
 * @property title stored title.
 * @property description stored description.
 * @property createdAtMillis creation instant in epoch millis.
 * @property updatedAtMillis last edit instant in epoch millis.
 */
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["createdAtMillis"])],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
