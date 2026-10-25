package com.jesse.taskapp.feature.tasklist.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesse.taskapp.feature.tasklist.R
import com.jesse.taskapp.feature.tasklist.presentation.TaskListItemUi
import com.jesse.taskapp.feature.tasklist.presentation.TaskListTags
import com.jesse.taskapp.ui.TaskAppTheme

/**
 * One task row.
 *
 * @param task row contents.
 * @param selectionMode true when the row should expose selected state.
 * @param onClick called for a tap.
 * @param onLongClick called for a long press.
 * @param modifier layout modifier.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    task: TaskListItemUi,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TaskListTags.row(task.id))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Text(
                text = task.createdAtLabel,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (selectionMode) {
                Text(
                    text = stringResource(
                        if (task.selected) R.string.task_selected else R.string.task_not_selected,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

/**
 * Preview of [TaskRow] with a description and no selection.
 */
@Preview(showBackground = true, name = "Row")
@Composable
private fun TaskRowPreview() {
    TaskAppTheme {
        TaskRow(
            task = previewItems().first(),
            selectionMode = false,
            onClick = {},
            onLongClick = {},
        )
    }
}

/**
 * Preview of [TaskRow] with an empty description.
 */
@Preview(showBackground = true, name = "Row without description")
@Composable
private fun TaskRowWithoutDescriptionPreview() {
    TaskAppTheme {
        TaskRow(
            task = previewItems()[1],
            selectionMode = false,
            onClick = {},
            onLongClick = {},
        )
    }
}

/**
 * Preview of [TaskRow] when it is part of a selection.
 */
@Preview(showBackground = true, name = "Selected row")
@Composable
private fun TaskRowSelectedPreview() {
    TaskAppTheme {
        TaskRow(
            task = previewItems().first().copy(selected = true),
            selectionMode = true,
            onClick = {},
            onLongClick = {},
        )
    }
}

/**
 * Preview of [TaskRow] in selection mode when it is not selected.
 */
@Preview(showBackground = true, name = "Unselected row")
@Composable
private fun TaskRowUnselectedInSelectionPreview() {
    TaskAppTheme {
        TaskRow(
            task = previewItems()[1],
            selectionMode = true,
            onClick = {},
            onLongClick = {},
        )
    }
}

/**
 * Sample rows for row previews.
 *
 * @return two tasks, the second without a description.
 */
private fun previewItems(): List<TaskListItemUi> {
    return listOf(
        TaskListItemUi(
            id = 1,
            title = "Buy milk",
            description = "Oat milk from the shop on the corner",
            createdAtLabel = "17 Oct 2026, 09:15",
            selected = false,
        ),
        TaskListItemUi(
            id = 2,
            title = "Write notes",
            description = "",
            createdAtLabel = "16 Oct 2026, 18:40",
            selected = false,
        ),
    )
}
