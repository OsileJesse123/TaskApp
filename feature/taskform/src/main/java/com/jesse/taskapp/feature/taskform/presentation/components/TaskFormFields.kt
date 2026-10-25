package com.jesse.taskapp.feature.taskform.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesse.taskapp.feature.taskform.R
import com.jesse.taskapp.feature.taskform.domain.DescriptionError
import com.jesse.taskapp.feature.taskform.domain.TaskInputRules
import com.jesse.taskapp.feature.taskform.domain.TitleError
import com.jesse.taskapp.feature.taskform.presentation.TaskFormTags
import com.jesse.taskapp.feature.taskform.presentation.TaskFormUiState
import com.jesse.taskapp.ui.TaskAppTheme

/**
 * Title, description, and save action.
 *
 * @param state field values and errors. Loading and not-found are not rendered here.
 * @param onTitleChange called with the new title.
 * @param onDescriptionChange called with the new description.
 * @param onSave called when the user saves.
 * @param modifier layout modifier.
 */
@Composable
fun TaskFormFields(
    state: TaskFormUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TaskFormTags.TITLE),
            label = { Text(stringResource(R.string.task_title_label)) },
            isError = state.titleError != null,
            supportingText = {
                val error = state.titleError
                if (error != null) {
                    Text(text = titleErrorText(error))
                }
            },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.description,
            onValueChange = onDescriptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TaskFormTags.DESCRIPTION),
            label = { Text(stringResource(R.string.task_description_label)) },
            isError = state.descriptionError != null,
            supportingText = {
                if (state.descriptionError != null) {
                    Text(text = descriptionErrorText())
                }
            },
            minLines = 4,
        )
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TaskFormTags.SAVE),
        ) {
            Text(stringResource(R.string.task_save))
        }
    }
}

/**
 * Maps a title error to copy that includes the shared length limit.
 *
 * @param error title problem from validation.
 * @return the message to show under the title field.
 */
@Composable
private fun titleErrorText(error: TitleError): String {
    return when (error) {
        TitleError.Required -> stringResource(R.string.task_title_required)
        TitleError.TooLong -> stringResource(R.string.task_title_too_long, TaskInputRules.TITLE_MAX_LENGTH)
    }
}

/**
 * Maps the description length error to copy that includes the shared limit.
 *
 * @return the message to show under the description field.
 */
@Composable
private fun descriptionErrorText(): String {
    return stringResource(
        R.string.task_description_too_long,
        TaskInputRules.DESCRIPTION_MAX_LENGTH,
    )
}

/**
 * Preview of [TaskFormFields] with both field errors at once.
 */
@Preview(showBackground = true, name = "Both errors")
@Composable
private fun TaskFormFieldsBothErrorsPreview() {
    TaskAppTheme {
        TaskFormFields(
            state = TaskFormUiState(
                titleError = TitleError.Required,
                descriptionError = DescriptionError.TooLong,
            ),
            onTitleChange = {},
            onDescriptionChange = {},
            onSave = {},
        )
    }
}
