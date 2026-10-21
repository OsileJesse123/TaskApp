package com.jesse.taskapp.feature.tasklist.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesse.taskapp.R
import com.jesse.taskapp.feature.tasklist.presentation.components.TaskFilterRow
import com.jesse.taskapp.feature.tasklist.presentation.components.TaskListBody
import com.jesse.taskapp.feature.tasklist.presentation.components.TaskListTopBar
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
