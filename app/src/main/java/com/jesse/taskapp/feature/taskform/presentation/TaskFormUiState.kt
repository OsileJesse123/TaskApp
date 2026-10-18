package com.jesse.taskapp.feature.taskform.presentation

import com.jesse.taskapp.feature.taskform.domain.DescriptionError
import com.jesse.taskapp.feature.taskform.domain.TitleError

/**
 * Rendered state of the create and edit form.
 *
 * Field errors stay here because the user must keep seeing them while they edit. A failed write
 * is a [TaskFormEvent] so it is not replayed after rotation.
 *
 * @property isEdit true when the route included a task id.
 * @property isLoading true while an existing task is being read.
 * @property notFound true when the id from the edit route is not stored.
 * @property loadFailed true when that read threw.
 * @property title current title field.
 * @property description current description field.
 * @property titleError title validation error, or null.
 * @property descriptionError description validation error, or null.
 */
data class TaskFormUiState(
    val isEdit: Boolean = false,
    val isLoading: Boolean = false,
    val notFound: Boolean = false,
    val loadFailed: Boolean = false,
    val title: String = "",
    val description: String = "",
    val titleError: TitleError? = null,
    val descriptionError: DescriptionError? = null,
)
