package com.jesse.taskapp.feature.tasklist.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesse.taskapp.feature.tasklist.R
import com.jesse.taskapp.feature.tasklist.presentation.TaskListContent
import com.jesse.taskapp.feature.tasklist.presentation.TaskListItemUi
import com.jesse.taskapp.ui.TaskAppTheme

/**
 * Body of the list under the filter controls.
 *
 * @param content which body to show.
 * @param selectionMode true when taps toggle selection instead of opening a task.
 * @param onTaskClick called with an id outside selection mode.
 * @param onStartSelection called with an id on long press.
 * @param onToggleSelected called with an id in selection mode.
 * @param modifier layout modifier, usually a weight so the list fills the scaffold.
 */
@Composable
fun TaskListBody(
    content: TaskListContent,
    selectionMode: Boolean,
    onTaskClick: (Long) -> Unit,
    onStartSelection: (Long) -> Unit,
    onToggleSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (content) {
        TaskListContent.Loading -> Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.task_loading),
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        TaskListContent.Empty -> TaskListMessage(
            title = stringResource(R.string.task_empty_title),
            body = stringResource(R.string.task_empty_body),
            modifier = modifier,
        )
        TaskListContent.NoResults -> TaskListMessage(
            title = stringResource(R.string.task_no_results_title),
            body = stringResource(R.string.task_no_results_body),
            modifier = modifier,
        )
        TaskListContent.Unavailable -> TaskListMessage(
            title = stringResource(R.string.task_unavailable_title),
            body = stringResource(R.string.task_unavailable_body),
            modifier = modifier,
        )
        is TaskListContent.Tasks -> LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = content.tasks, key = TaskListItemUi::id) { task ->
                TaskRow(
                    task = task,
                    selectionMode = selectionMode,
                    onClick = {
                        if (selectionMode) onToggleSelected(task.id) else onTaskClick(task.id)
                    },
                    onLongClick = { onStartSelection(task.id) },
                )
            }
        }
    }
}

/**
 * Preview of [TaskListBody] while the first load has not arrived.
 */
@Preview(showBackground = true, name = "Loading body")
@Composable
private fun TaskListBodyLoadingPreview() {
    TaskAppTheme {
        TaskListBody(
            content = TaskListContent.Loading,
            selectionMode = false,
            onTaskClick = {},
            onStartSelection = {},
            onToggleSelected = {},
        )
    }
}

/**
 * Preview of [TaskListBody] when nothing is stored and no filter is active.
 */
@Preview(showBackground = true, name = "Empty body")
@Composable
private fun TaskListBodyEmptyPreview() {
    TaskAppTheme {
        TaskListBody(
            content = TaskListContent.Empty,
            selectionMode = false,
            onTaskClick = {},
            onStartSelection = {},
            onToggleSelected = {},
        )
    }
}

/**
 * Preview of [TaskListBody] when the filter matches nothing.
 */
@Preview(showBackground = true, name = "No results body")
@Composable
private fun TaskListBodyNoResultsPreview() {
    TaskAppTheme {
        TaskListBody(
            content = TaskListContent.NoResults,
            selectionMode = false,
            onTaskClick = {},
            onStartSelection = {},
            onToggleSelected = {},
        )
    }
}

/**
 * Preview of [TaskListBody] when the task stream failed.
 */
@Preview(showBackground = true, name = "Unavailable body")
@Composable
private fun TaskListBodyUnavailablePreview() {
    TaskAppTheme {
        TaskListBody(
            content = TaskListContent.Unavailable,
            selectionMode = false,
            onTaskClick = {},
            onStartSelection = {},
            onToggleSelected = {},
        )
    }
}

/**
 * Preview of [TaskListBody] with tasks.
 */
@Preview(showBackground = true, name = "Tasks body")
@Composable
private fun TaskListBodyTasksPreview() {
    TaskAppTheme {
        TaskListBody(
            content = TaskListContent.Tasks(previewItems()),
            selectionMode = false,
            onTaskClick = {},
            onStartSelection = {},
            onToggleSelected = {},
        )
    }
}

/**
 * Sample rows for the tasks-body preview.
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
