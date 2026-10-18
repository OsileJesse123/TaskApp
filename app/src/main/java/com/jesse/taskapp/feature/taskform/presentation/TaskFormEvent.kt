package com.jesse.taskapp.feature.taskform.presentation

/**
 * One-shot effects for the task form.
 *
 * Validation stays on [TaskFormUiState]. This channel is for a write failure or a completed save,
 * neither of which should replay after rotation.
 */
sealed interface TaskFormEvent {
    /**
     * The task could not be loaded for editing.
     */
    data object ShowLoadError : TaskFormEvent

    /**
     * The write threw, or the id disappeared before update.
     */
    data object ShowSaveError : TaskFormEvent

    /**
     * The task was saved. The route leaves the form.
     */
    data object TaskSaved : TaskFormEvent
}
