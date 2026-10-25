package com.jesse.taskapp.feature.tasklist.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jesse.taskapp.feature.tasklist.R

/**
 * Connects [TaskListViewModel] to [TaskListScreen].
 *
 * This is the only list composable that knows about the ViewModel. It is not previewed.
 * [TaskListScreen] takes state and callbacks so it can be previewed and tested on its own.
 *
 * @param onOpenTask called with the id to open. The task itself is not passed.
 * @param onCreateTask called when the user starts a new task.
 * @param viewModel list state holder. Tests of the screen do not use this default.
 */
@Composable
fun TaskListRoute(
    onOpenTask: (Long) -> Unit,
    onCreateTask: () -> Unit,
    viewModel: TaskListViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                TaskListEvent.ShowLoadError -> context.getString(R.string.task_load_error)
                TaskListEvent.ShowDeleteError -> context.getString(R.string.task_delete_error)
            }
            snackbarHostState.showSnackbar(message)
        }
    }
    TaskListScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onQueryChange = viewModel::onQueryChange,
        onCreationDateSelected = viewModel::onCreationDateSelected,
        onCreationDateCleared = viewModel::onCreationDateCleared,
        onTaskClick = onOpenTask,
        onStartSelection = viewModel::onStartSelection,
        onToggleSelected = viewModel::onToggleSelected,
        onCancelSelection = viewModel::onCancelSelection,
        onDeleteSelection = viewModel::onDeleteSelection,
        onConfirmDelete = viewModel::onConfirmDelete,
        onDismissDelete = viewModel::onDismissDelete,
        onCreate = onCreateTask,
    )
}
