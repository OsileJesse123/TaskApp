package com.jesse.taskapp

import com.jesse.taskapp.core.domain.model.Task

/**
 * Builds a task with short defaults so tests only set what they care about.
 *
 * @param id row id.
 * @param title title. Defaults to a readable name that includes [id].
 * @param description description. Defaults to empty.
 * @param createdAtMillis creation instant.
 * @param updatedAtMillis edit instant. Defaults to the creation instant.
 * @return a domain task.
 */
fun sampleTask(
    id: Long,
    title: String = "Task $id",
    description: String = "",
    createdAtMillis: Long = 1_700_000_000_000,
    updatedAtMillis: Long = createdAtMillis,
): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}
