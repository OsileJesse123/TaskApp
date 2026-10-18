package com.jesse.taskapp.feature.tasklist.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesse.taskapp.R
import com.jesse.taskapp.ui.theme.TaskAppTheme

/**
 * Task list that does not read a ViewModel or a navigation controller.
 *
 * The date-picker dialog is local UI. The selected day itself comes from [state], because that
 * is what the list is filtered by.
 *
 * @param state rendered list state.
 * @param snackbarHostState host for one-shot messages collected by the route.
 * @param onQueryChange called with the new search text.
 * @param onCreationDateSelected called with the picker UTC midnight.
 * @param onCreationDateCleared called when the user clears the day filter.
 * @param onTaskClick called with a task id when the user opens it.
 * @param onStartSelection called with a task id on long press.
 * @param onToggleSelected called with a task id while selection mode is on.
 * @param onCancelSelection called when the user leaves selection mode.
 * @param onDeleteSelection called when the user asks to delete the selection.
 * @param onConfirmDelete called when the user confirms bulk delete.
 * @param onDismissDelete called when the user dismisses the confirm dialog.
 * @param onCreate called when the user wants a new task.
 * @param modifier optional layout modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    state: TaskListUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onQueryChange: (String) -> Unit = {},
    onCreationDateSelected: (Long) -> Unit = {},
    onCreationDateCleared: () -> Unit = {},
    onTaskClick: (Long) -> Unit = {},
    onStartSelection: (Long) -> Unit = {},
    onToggleSelected: (Long) -> Unit = {},
    onCancelSelection: () -> Unit = {},
    onDeleteSelection: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onDismissDelete: () -> Unit = {},
    onCreate: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // The picker is chrome. Keeping it out of UiState avoids restoring an open dialog as business state.
    var showDatePicker by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TaskListTopBar(
                selectionMode = state.selectionMode,
                selectedCount = state.selectedCount,
                onCancelSelection = onCancelSelection,
                onDeleteSelection = onDeleteSelection,
            )
        },
        floatingActionButton = {
            if (!state.selectionMode) {
                FloatingActionButton(
                    onClick = onCreate,
                    modifier = Modifier.testTag(TaskListTags.CREATE),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.task_create),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TaskListTags.SEARCH),
                label = { Text(stringResource(R.string.task_search_label)) },
                singleLine = true,
            )
            TaskFilterRow(
                createdOnLabel = state.createdOnLabel,
                onOpenPicker = { showDatePicker = true },
                onClear = onCreationDateCleared,
            )
            TaskListBody(
                content = state.content,
                selectionMode = state.selectionMode,
                onTaskClick = onTaskClick,
                onStartSelection = onStartSelection,
                onToggleSelected = onToggleSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.createdOnUtcMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let(onCreationDateSelected)
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.task_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.task_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.task_delete_confirm_title)) },
            text = { Text(stringResource(R.string.task_delete_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    modifier = Modifier.testTag(TaskListTags.CONFIRM_DELETE),
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
 * Top bar for browsing or for an active selection.
 *
 * @param selectionMode true when at least one row is selected.
 * @param selectedCount number of selected rows.
 * @param onCancelSelection called from the close action.
 * @param onDeleteSelection called from the delete action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListTopBar(
    selectionMode: Boolean,
    selectedCount: Int,
    onCancelSelection: () -> Unit,
    onDeleteSelection: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = if (selectionMode) {
                    stringResource(R.string.task_selected_count, selectedCount)
                } else {
                    stringResource(R.string.task_list_title)
                },
            )
        },
        navigationIcon = {
            if (selectionMode) {
                IconButton(onClick = onCancelSelection) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.task_close_selection),
                    )
                }
            }
        },
        actions = {
            if (selectionMode) {
                IconButton(
                    onClick = onDeleteSelection,
                    modifier = Modifier.testTag(TaskListTags.DELETE),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.task_delete),
                    )
                }
            }
        },
    )
}

/**
 * Creation-date filter control.
 *
 * @param createdOnLabel formatted selected day, or null when the list is unfiltered.
 * @param onOpenPicker called when the user wants to pick a day.
 * @param onClear called when the user clears the day.
 */
@Composable
fun TaskFilterRow(
    createdOnLabel: String?,
    onOpenPicker: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onOpenPicker,
            modifier = Modifier.testTag(TaskListTags.FILTER),
        ) {
            Text(
                text = if (createdOnLabel == null) {
                    stringResource(R.string.task_filter_by_date)
                } else {
                    stringResource(R.string.task_filtered_on, createdOnLabel)
                },
            )
        }
        if (createdOnLabel != null) {
            TextButton(
                onClick = onClear,
                modifier = Modifier.testTag(TaskListTags.CLEAR_FILTER),
            ) {
                Text(stringResource(R.string.task_clear_date_filter))
            }
        }
    }
}

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
 * Centered title and supporting line for an empty or failed list.
 *
 * @param title primary message.
 * @param body secondary message.
 * @param modifier layout modifier.
 */
@Composable
fun TaskListMessage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = body, style = MaterialTheme.typography.bodyMedium)
    }
}

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
 * Preview of [TaskListScreen] while the first load has not arrived.
 */
@Preview(showBackground = true, name = "Loading")
@Composable
private fun TaskListScreenLoadingPreview() {
    TaskAppTheme {
        TaskListScreen(state = TaskListUiState(content = TaskListContent.Loading))
    }
}

/**
 * Preview of [TaskListScreen] when the database has no tasks and no filter is active.
 */
@Preview(showBackground = true, name = "Empty")
@Composable
private fun TaskListScreenEmptyPreview() {
    TaskAppTheme {
        TaskListScreen(state = TaskListUiState(content = TaskListContent.Empty))
    }
}

/**
 * Preview of [TaskListScreen] when a search or date matches nothing.
 */
@Preview(showBackground = true, name = "No results")
@Composable
private fun TaskListScreenNoResultsPreview() {
    TaskAppTheme {
        TaskListScreen(
            state = TaskListUiState(
                query = "milk",
                createdOnUtcMillis = 1_760_659_200_000,
                createdOnLabel = "17 Oct 2026",
                content = TaskListContent.NoResults,
            ),
        )
    }
}

/**
 * Preview of [TaskListScreen] when the task stream failed.
 */
@Preview(showBackground = true, name = "Unavailable")
@Composable
private fun TaskListScreenUnavailablePreview() {
    TaskAppTheme {
        TaskListScreen(state = TaskListUiState(content = TaskListContent.Unavailable))
    }
}

/**
 * Preview of [TaskListScreen] with tasks and no selection.
 */
@Preview(showBackground = true, name = "Content")
@Composable
private fun TaskListScreenContentPreview() {
    TaskAppTheme {
        TaskListScreen(state = previewListState(selectionMode = false))
    }
}

/**
 * Preview of [TaskListScreen] while two tasks are selected and the confirm dialog is closed.
 */
@Preview(showBackground = true, name = "Selection")
@Composable
private fun TaskListScreenSelectionPreview() {
    TaskAppTheme {
        TaskListScreen(state = previewListState(selectionMode = true))
    }
}

/**
 * Preview of [TaskListScreen] with the bulk-delete dialog open.
 */
@Preview(showBackground = true, name = "Delete confirmation")
@Composable
private fun TaskListScreenDeleteConfirmationPreview() {
    TaskAppTheme {
        TaskListScreen(state = previewListState(selectionMode = true).copy(showDeleteConfirmation = true))
    }
}

/**
 * Preview of [TaskListTopBar] while the user is browsing.
 */
@Preview(showBackground = true, name = "Browsing")
@Composable
private fun TaskListTopBarBrowsingPreview() {
    TaskAppTheme {
        TaskListTopBar(
            selectionMode = false,
            selectedCount = 0,
            onCancelSelection = {},
            onDeleteSelection = {},
        )
    }
}

/**
 * Preview of [TaskListTopBar] during selection.
 */
@Preview(showBackground = true, name = "Selecting")
@Composable
private fun TaskListTopBarSelectingPreview() {
    TaskAppTheme {
        TaskListTopBar(
            selectionMode = true,
            selectedCount = 2,
            onCancelSelection = {},
            onDeleteSelection = {},
        )
    }
}

/**
 * Preview of [TaskFilterRow] with no day selected.
 */
@Preview(showBackground = true, name = "No date")
@Composable
private fun TaskFilterRowEmptyPreview() {
    TaskAppTheme {
        TaskFilterRow(createdOnLabel = null, onOpenPicker = {}, onClear = {})
    }
}

/**
 * Preview of [TaskFilterRow] with a creation day selected.
 */
@Preview(showBackground = true, name = "Date selected")
@Composable
private fun TaskFilterRowSelectedPreview() {
    TaskAppTheme {
        TaskFilterRow(createdOnLabel = "17 Oct 2026", onOpenPicker = {}, onClear = {})
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
 * Preview of [TaskListMessage].
 */
@Preview(showBackground = true, name = "Message")
@Composable
private fun TaskListMessagePreview() {
    TaskAppTheme {
        TaskListMessage(title = "No tasks yet", body = "Create a task to keep track of what you want to do.")
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
 * Builds a content state used by list previews.
 *
 * @param selectionMode true to mark the first row selected.
 * @return a populated list state.
 */
private fun previewListState(selectionMode: Boolean): TaskListUiState {
    val items = previewItems().mapIndexed { index, item ->
        item.copy(selected = selectionMode && index == 0)
    }
    return TaskListUiState(
        selectionMode = selectionMode,
        selectedCount = if (selectionMode) 1 else 0,
        content = TaskListContent.Tasks(items),
    )
}

/**
 * Sample rows for previews.
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
