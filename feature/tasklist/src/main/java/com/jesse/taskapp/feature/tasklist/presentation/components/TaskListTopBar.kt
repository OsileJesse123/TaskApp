package com.jesse.taskapp.feature.tasklist.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jesse.taskapp.feature.tasklist.R
import com.jesse.taskapp.feature.tasklist.presentation.TaskListTags
import com.jesse.taskapp.ui.TaskAppTheme

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
