package com.jesse.taskapp.feature.taskform.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jesse.taskapp.feature.taskform.R
import com.jesse.taskapp.feature.taskform.domain.DescriptionError
import com.jesse.taskapp.feature.taskform.domain.TaskInputRules
import com.jesse.taskapp.feature.taskform.domain.TitleError
import com.jesse.taskapp.feature.taskform.presentation.components.TaskFormFields
import com.jesse.taskapp.feature.taskform.presentation.components.TaskFormStatus
import com.jesse.taskapp.ui.TaskAppTheme

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
