package com.jesse.taskapp.feature.taskdetail.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jesse.taskapp.R
import com.jesse.taskapp.feature.taskdetail.presentation.components.TaskDetailContent
import com.jesse.taskapp.feature.taskdetail.presentation.components.TaskDetailStatus
import com.jesse.taskapp.ui.theme.TaskAppTheme

/**
 * Task detail that does not read a ViewModel or a navigation controller.
 *
 * @param state rendered detail state.
 * @param snackbarHostState host for one-shot messages collected by the route.
 * @param onBack called from the top bar.
 * @param onEdit called when the user wants to edit this task.
 * @param onDeleteClick called when the user asks to delete.
 * @param onConfirmDelete called when the user confirms deletion.
 * @param onDismissDelete called when the user dismisses the dialog.
 * @param modifier optional layout modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    state: TaskDetailUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onDismissDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.task_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.task_back),
                        )
                    }
                },
                actions = {
                    if (state is TaskDetailUiState.Content) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.testTag(TaskDetailTags.DELETE),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.task_delete),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when (state) {
            TaskDetailUiState.Loading -> TaskDetailStatus(
                title = stringResource(R.string.task_loading),
                body = null,
                showProgress = true,
                modifier = Modifier.padding(innerPadding),
            )
            TaskDetailUiState.NotFound -> TaskDetailStatus(
                title = stringResource(R.string.task_not_found_title),
                body = stringResource(R.string.task_not_found_body),
                showProgress = false,
                modifier = Modifier.padding(innerPadding),
            )
            TaskDetailUiState.LoadFailed -> TaskDetailStatus(
                title = stringResource(R.string.task_load_failed_title),
                body = stringResource(R.string.task_load_failed_body),
                showProgress = false,
                modifier = Modifier.padding(innerPadding),
            )
            is TaskDetailUiState.Content -> TaskDetailContent(
                state = state,
                onEdit = onEdit,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
    val content = state as? TaskDetailUiState.Content
    if (content?.showDeleteConfirmation == true) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.task_delete_one_title)) },
            text = { Text(stringResource(R.string.task_delete_one_body)) },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    modifier = Modifier.testTag(TaskDetailTags.CONFIRM_DELETE),
                ) {
                    Text(stringResource(R.string.task_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text(stringResource(R.string.task_cancel))
                }
            },
        )
    }
}

/**
 * Preview of [TaskDetailScreen] while the row is loading.
 */
@Preview(showBackground = true, name = "Loading")
@Composable
private fun TaskDetailScreenLoadingPreview() {
    TaskAppTheme {
        TaskDetailScreen(state = TaskDetailUiState.Loading)
    }
}

/**
 * Preview of [TaskDetailScreen] when the id is not stored.
 */
@Preview(showBackground = true, name = "Not found")
@Composable
private fun TaskDetailScreenNotFoundPreview() {
    TaskAppTheme {
        TaskDetailScreen(state = TaskDetailUiState.NotFound)
    }
}

/**
 * Preview of [TaskDetailScreen] when the load failed.
 */
@Preview(showBackground = true, name = "Load failed")
@Composable
private fun TaskDetailScreenLoadFailedPreview() {
    TaskAppTheme {
        TaskDetailScreen(state = TaskDetailUiState.LoadFailed)
    }
}

/**
 * Preview of [TaskDetailScreen] with a stored task and a description.
 */
@Preview(showBackground = true, name = "Content")
@Composable
private fun TaskDetailScreenContentPreview() {
    TaskAppTheme {
        TaskDetailScreen(state = previewDetail(description = "Oat milk from the shop"))
    }
}

/**
 * Preview of [TaskDetailScreen] when the description was left blank.
 */
@Preview(showBackground = true, name = "No description")
@Composable
private fun TaskDetailScreenNoDescriptionPreview() {
    TaskAppTheme {
        TaskDetailScreen(state = previewDetail(description = ""))
    }
}

/**
 * Preview of [TaskDetailScreen] with the delete dialog open.
 */
@Preview(showBackground = true, name = "Delete confirmation")
@Composable
private fun TaskDetailScreenDeletePreview() {
    TaskAppTheme {
        TaskDetailScreen(state = previewDetail(description = "Oat milk").copy(showDeleteConfirmation = true))
    }
}

/**
 * Sample content for detail previews.
 *
 * @param description description to show.
 * @return content with the dialog closed.
 */
private fun previewDetail(description: String): TaskDetailUiState.Content {
    return TaskDetailUiState.Content(
        title = "Buy milk",
        description = description,
        createdAtLabel = "17 Oct 2026, 09:15",
        updatedAtLabel = "17 Oct 2026, 11:02",
        showDeleteConfirmation = false,
    )
}
