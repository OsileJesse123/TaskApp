package com.jesse.taskapp.feature.taskdetail.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jesse.taskapp.ui.TaskAppTheme

/**
 * One labeled timestamp.
 *
 * @param label "Created" or "Edited".
 * @param value formatted time.
 */
@Composable
fun TaskDetailDateLine(
    label: String,
    value: String,
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Preview of [TaskDetailDateLine].
 */
@Preview(showBackground = true, name = "Date line")
@Composable
private fun TaskDetailDateLinePreview() {
    TaskAppTheme {
        TaskDetailDateLine(label = "Created", value = "17 Oct 2026, 09:15")
    }
}
