package com.jesse.taskapp.feature.taskform.presentation.components

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
import com.jesse.taskapp.ui.TaskAppTheme

/**
 * Centered status for loading, not found, and load failure on the form.
 *
 * @param title primary message.
 * @param body optional supporting message.
 * @param showProgress true to show a spinner.
 * @param modifier layout modifier.
 */
@Composable
fun TaskFormStatus(
    title: String,
    body: String? = null,
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
 * Preview of [TaskFormStatus] for a missing task.
 */
@Preview(showBackground = true, name = "Status")
@Composable
private fun TaskFormStatusPreview() {
    TaskAppTheme {
        TaskFormStatus(
            title = "Task not found",
            body = "It may have been deleted.",
            showProgress = false,
        )
    }
}

/**
 * Preview of [TaskFormStatus] while loading.
 */
@Preview(showBackground = true, name = "Status loading")
@Composable
private fun TaskFormStatusLoadingPreview() {
    TaskAppTheme {
        TaskFormStatus(title = "Loading task", showProgress = true)
    }
}
