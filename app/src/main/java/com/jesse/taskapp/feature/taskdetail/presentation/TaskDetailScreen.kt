package com.jesse.taskapp.feature.taskdetail.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesse.taskapp.R
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
 * Centered status for loading, not found, and load failure.
 *
 * @param title primary message.
 * @param body optional supporting message.
 * @param showProgress true to show a spinner above the title.
 * @param modifier layout modifier.
 */
@Composable
fun TaskDetailStatus(
    title: String,
    body: String?,
    showProgress: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showProgress) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
        }
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (body != null) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/**
 * Title, description, dates, and the edit action for a stored task.
 *
 * @param state content to render.
 * @param onEdit called when the user edits this task.
 * @param modifier layout modifier.
 */
@Composable
fun TaskDetailContent(
    state: TaskDetailUiState.Content,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = state.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag(TaskDetailTags.TITLE),
        )
        Text(
            text = stringResource(R.string.task_description_heading),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = state.description.ifBlank { stringResource(R.string.task_no_description) },
            style = MaterialTheme.typography.bodyLarge,
        )
        TaskDetailDateLine(
            label = stringResource(R.string.task_created_label),
            value = state.createdAtLabel,
        )
        TaskDetailDateLine(
            label = stringResource(R.string.task_edited_label),
            value = state.updatedAtLabel,
        )
        Button(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TaskDetailTags.EDIT),
        ) {
            Text(stringResource(R.string.task_edit))
        }
    }
}

/**
 * One labeled timestamp.
 *
 * @param label "Created" or "Edited".
 * @param value formatted time.
 */
@Composable
fun TaskDetailDateLine(
    label: String,
    value: String,
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
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
 * Preview of [TaskDetailStatus] without a spinner.
 */
@Preview(showBackground = true, name = "Status")
@Composable
private fun TaskDetailStatusPreview() {
    TaskAppTheme {
        TaskDetailStatus(
            title = "Task not found",
            body = "It may have been deleted.",
            showProgress = false,
        )
    }
}

/**
 * Preview of [TaskDetailStatus] while loading.
 */
@Preview(showBackground = true, name = "Status loading")
@Composable
private fun TaskDetailStatusLoadingPreview() {
    TaskAppTheme {
        TaskDetailStatus(title = "Loading tasks", body = null, showProgress = true)
    }
}

/**
 * Preview of [TaskDetailContent].
 */
@Preview(showBackground = true, name = "Content block")
@Composable
private fun TaskDetailContentPreview() {
    TaskAppTheme {
        TaskDetailContent(state = previewDetail(description = "Oat milk"), onEdit = {})
    }
}

/**
 * Preview of [TaskDetailDateLine].
 */
@Preview(showBackground = true, name = "Date line")
@Composable
private fun TaskDetailDateLinePreview() {
    TaskAppTheme {
        TaskDetailDateLine(label = "Created", value = "17 Oct 2026, 09:15")
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
