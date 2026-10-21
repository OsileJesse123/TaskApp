package com.jesse.taskapp.feature.tasklist.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesse.taskapp.R
import com.jesse.taskapp.feature.tasklist.presentation.TaskListTags
import com.jesse.taskapp.ui.theme.TaskAppTheme

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
