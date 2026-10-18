package com.jesse.taskapp.navigation

import kotlinx.serialization.Serializable

/**
 * Task list, the start destination.
 */
@Serializable
data object TaskListDestination

/**
 * Detail for one task.
 *
 * @property taskId id to load from local storage. The task object is not an argument.
 */
@Serializable
data class TaskDetailDestination(
    val taskId: Long,
)

/**
 * Form for a new task. There is no id argument.
 */
@Serializable
data object TaskCreateDestination

/**
 * Form for an existing task.
 *
 * @property taskId id to load into the form.
 */
@Serializable
data class TaskEditDestination(
    val taskId: Long,
)
