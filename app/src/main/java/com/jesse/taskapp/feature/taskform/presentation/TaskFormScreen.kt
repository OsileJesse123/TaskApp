package com.jesse.taskapp.feature.taskform.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesse.taskapp.R
import com.jesse.taskapp.feature.taskform.domain.DescriptionError
import com.jesse.taskapp.feature.taskform.domain.TaskInputRules
import com.jesse.taskapp.feature.taskform.domain.TitleError
import com.jesse.taskapp.ui.theme.TaskAppTheme

/**
 * Create and edit form that does not read a ViewModel or a navigation controller.
 *
 * @param state rendered form state.
 * @param snackbarHostState host for one-shot messages collected by the route.
 * @param onTitleChange called with the new title.
 * @param onDescriptionChange called with the new description.
 * @param onSave called when the user saves.
 * @param onBack called from the top bar.
 * @param modifier optional layout modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    state: TaskFormUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onTitleChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (state.isEdit) R.string.task_form_edit_title else R.string.task_form_create_title,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.task_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> TaskFormStatus(
                title = stringResource(R.string.task_loading_form),
                showProgress = true,
                modifier = Modifier.padding(innerPadding),
            )
            state.notFound -> TaskFormStatus(
                title = stringResource(R.string.task_not_found_title),
                body = stringResource(R.string.task_not_found_body),
                showProgress = false,
                modifier = Modifier.padding(innerPadding),
            )
            state.loadFailed -> TaskFormStatus(
                title = stringResource(R.string.task_load_failed_title),
                body = stringResource(R.string.task_load_failed_body),
                showProgress = false,
                modifier = Modifier.padding(innerPadding),
            )
            else -> TaskFormFields(
                state = state,
                onTitleChange = onTitleChange,
                onDescriptionChange = onDescriptionChange,
                onSave = onSave,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

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
 * Preview of [TaskFormScreen] for a new task with empty fields.
 */
@Preview(showBackground = true, name = "Create")
@Composable
private fun TaskFormScreenCreatePreview() {
    TaskAppTheme {
        TaskFormScreen(state = TaskFormUiState(isEdit = false))
    }
}

/**
 * Preview of [TaskFormScreen] while an existing task is loading.
 */
@Preview(showBackground = true, name = "Loading")
@Composable
private fun TaskFormScreenLoadingPreview() {
    TaskAppTheme {
        TaskFormScreen(state = TaskFormUiState(isEdit = true, isLoading = true))
    }
}

/**
 * Preview of [TaskFormScreen] when the edit id is missing.
 */
@Preview(showBackground = true, name = "Not found")
@Composable
private fun TaskFormScreenNotFoundPreview() {
    TaskAppTheme {
        TaskFormScreen(state = TaskFormUiState(isEdit = true, notFound = true))
    }
}

/**
 * Preview of [TaskFormScreen] when loading the task threw.
 */
@Preview(showBackground = true, name = "Load failed")
@Composable
private fun TaskFormScreenLoadFailedPreview() {
    TaskAppTheme {
        TaskFormScreen(state = TaskFormUiState(isEdit = true, loadFailed = true))
    }
}

/**
 * Preview of [TaskFormScreen] filled for editing, with no field errors.
 */
@Preview(showBackground = true, name = "Edit")
@Composable
private fun TaskFormScreenEditPreview() {
    TaskAppTheme {
        TaskFormScreen(
            state = TaskFormUiState(
                isEdit = true,
                title = "Buy milk",
                description = "Oat milk",
            ),
        )
    }
}

/**
 * Preview of [TaskFormScreen] when the title is blank.
 */
@Preview(showBackground = true, name = "Title required")
@Composable
private fun TaskFormScreenTitleRequiredPreview() {
    TaskAppTheme {
        TaskFormScreen(
            state = TaskFormUiState(
                title = " ",
                titleError = TitleError.Required,
            ),
        )
    }
}

/**
 * Preview of [TaskFormScreen] when the title is too long.
 */
@Preview(showBackground = true, name = "Title too long")
@Composable
private fun TaskFormScreenTitleTooLongPreview() {
    TaskAppTheme {
        TaskFormScreen(
            state = TaskFormUiState(
                title = "x".repeat(TaskInputRules.TITLE_MAX_LENGTH + 1),
                titleError = TitleError.TooLong,
            ),
        )
    }
}

/**
 * Preview of [TaskFormScreen] when the description is too long.
 */
@Preview(showBackground = true, name = "Description too long")
@Composable
private fun TaskFormScreenDescriptionTooLongPreview() {
    TaskAppTheme {
        TaskFormScreen(
            state = TaskFormUiState(
                title = "Buy milk",
                description = "x".repeat(40),
                descriptionError = DescriptionError.TooLong,
            ),
        )
    }
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
