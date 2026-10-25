package com.jesse.taskapp.core.data.mapper

import com.jesse.taskapp.core.data.local.TaskEntity
import com.jesse.taskapp.core.domain.model.Task

/**
 * Copies a Room row into the domain model.
 *
 * @receiver stored row.
 * @return domain task with the same values.
 */
fun TaskEntity.toTask(): Task {
    // Field names match on purpose so the mapping stays a direct copy.
    return Task(
        id = id,
        title = title,
        description = description,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}
