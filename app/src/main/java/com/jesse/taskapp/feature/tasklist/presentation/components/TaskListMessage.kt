package com.jesse.taskapp.feature.tasklist.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesse.taskapp.ui.theme.TaskAppTheme

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
 * Preview of [TaskListMessage].
 */
@Preview(showBackground = true, name = "Message")
@Composable
private fun TaskListMessagePreview() {
    TaskAppTheme {
        TaskListMessage(title = "No tasks yet", body = "Create a task to keep track of what you want to do.")
    }
}
