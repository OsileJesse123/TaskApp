package com.jesse.taskapp.feature.taskdetail.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesse.taskapp.ui.theme.TaskAppTheme

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
