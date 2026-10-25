package com.jesse.taskapp.feature.taskform.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jesse.taskapp.feature.taskform.R
import com.jesse.taskapp.core.navigation.TASK_ID_KEY

/**
 * Connects [TaskFormViewModel] to [TaskFormScreen].
 *
 * Create and edit share this route. The edit destination puts [TASK_ID_KEY] in saved state; create
 * does not. The screen never receives a task object.
 *
 * @param onBack called when the user leaves the form.
 * @param onSaved called after a successful write so the host can pop this destination.
 * @param viewModel form state holder.
 */
@Composable
fun TaskFormRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: TaskFormViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                TaskFormEvent.ShowLoadError ->
                    snackbarHostState.showSnackbar(context.getString(R.string.task_detail_load_error))
                TaskFormEvent.ShowSaveError ->
                    snackbarHostState.showSnackbar(context.getString(R.string.task_save_error))
                TaskFormEvent.TaskSaved -> onSaved()
            }
        }
    }
    TaskFormScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onSave = viewModel::onSave,
        onBack = onBack,
    )
}
