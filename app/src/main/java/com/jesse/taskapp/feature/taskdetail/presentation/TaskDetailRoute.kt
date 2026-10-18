package com.jesse.taskapp.feature.taskdetail.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jesse.taskapp.R

/**
 * Connects [TaskDetailViewModel] to [TaskDetailScreen].
 *
 * The route is not previewed. The screen takes state and callbacks only.
 *
 * @param onBack called when the user leaves without a successful delete.
 * @param onEdit called with no payload. The nav host already has the id for the edit route.
 * @param onDeleted called after the task is removed, so the host can return to the list.
 * @param viewModel detail state holder.
 */
@Composable
fun TaskDetailRoute(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                TaskDetailEvent.ShowLoadError ->
                    snackbarHostState.showSnackbar(context.getString(R.string.task_detail_load_error))
                TaskDetailEvent.ShowDeleteError ->
                    snackbarHostState.showSnackbar(context.getString(R.string.task_delete_error))
                TaskDetailEvent.TaskDeleted -> onDeleted()
            }
        }
    }
    TaskDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onEdit = onEdit,
        onDeleteClick = viewModel::onDeleteClick,
        onConfirmDelete = viewModel::onConfirmDelete,
        onDismissDelete = viewModel::onDismissDelete,
    )
}
