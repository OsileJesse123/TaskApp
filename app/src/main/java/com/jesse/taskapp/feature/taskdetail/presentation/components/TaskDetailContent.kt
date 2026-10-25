package com.jesse.taskapp.feature.taskdetail.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesse.taskapp.R
import com.jesse.taskapp.feature.taskdetail.presentation.TaskDetailTags
import com.jesse.taskapp.feature.taskdetail.presentation.TaskDetailUiState
import com.jesse.taskapp.ui.TaskAppTheme

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
 * Sample content for the content-block preview.
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
