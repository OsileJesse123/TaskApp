package com.jesse.taskapp.feature.taskform.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jesse.taskapp.feature.taskform.domain.TaskInputRules
import com.jesse.taskapp.feature.taskform.domain.TitleError
import com.jesse.taskapp.ui.TaskAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Instrumentation tests for [TaskFormScreen].
 */
class TaskFormScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    /**
     * When the title is blank, then the required message is shown.
     */
    @Test
    fun whenTitleIsBlank_thenRequiredMessageIsShown() {
        composeRule.setContent {
            TaskAppTheme {
                TaskFormScreen(
                    state = TaskFormUiState(titleError = TitleError.Required),
                )
            }
        }

        composeRule.onNodeWithText("Title is required").assertIsDisplayed()
    }

    /**
     * When the title is too long, then the message includes the shared limit.
     */
    @Test
    fun whenTitleIsTooLong_thenLimitIsShown() {
        composeRule.setContent {
            TaskAppTheme {
                TaskFormScreen(state = TaskFormUiState(titleError = TitleError.TooLong))
            }
        }

        composeRule.onNodeWithText("Title must be ${TaskInputRules.TITLE_MAX_LENGTH} characters or fewer")
            .assertIsDisplayed()
    }

    /**
     * When the user saves a filled create form, then save is reported.
     */
    @Test
    fun whenSaveIsPressed_thenSaveCallbackRuns() {
        var saved = false
        composeRule.setContent {
            TaskAppTheme {
                TaskFormScreen(
                    state = TaskFormUiState(title = "Buy milk"),
                    onSave = { saved = true },
                )
            }
        }

        composeRule.onNodeWithTag(TaskFormTags.SAVE).performClick()

        assertEquals(true, saved)
    }

    /**
     * When the edit id is missing, then the not-found message is shown.
     */
    @Test
    fun whenEditTaskIsMissing_thenNotFoundMessageIsShown() {
        composeRule.setContent {
            TaskAppTheme {
                TaskFormScreen(state = TaskFormUiState(isEdit = true, notFound = true))
            }
        }

        composeRule.onNodeWithText("Task not found").assertIsDisplayed()
    }
}
