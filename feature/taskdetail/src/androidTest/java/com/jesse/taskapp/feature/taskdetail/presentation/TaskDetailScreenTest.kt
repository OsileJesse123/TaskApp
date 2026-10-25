package com.jesse.taskapp.feature.taskdetail.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jesse.taskapp.ui.TaskAppTheme
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

/**
 * Instrumentation tests for [TaskDetailScreen].
 */
class TaskDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    /**
     * When the id is missing, then the not-found message is shown.
     */
    @Test
    fun whenTaskIsMissing_thenNotFoundMessageIsShown() {
        composeRule.setContent {
            TaskAppTheme {
                TaskDetailScreen(state = TaskDetailUiState.NotFound)
            }
        }

        composeRule.onNodeWithText("Task not found").assertIsDisplayed()
    }

    /**
     * When a task is loaded, then its title, dates, and edit action are shown.
     */
    @Test
    fun whenTaskIsLoaded_thenTitleAndEditAreShown() {
        var edited = false
        composeRule.setContent {
            TaskAppTheme {
                TaskDetailScreen(
                    state = TaskDetailUiState.Content(
                        title = "Buy milk",
                        description = "",
                        createdAtLabel = "17 Oct 2026, 09:15",
                        updatedAtLabel = "17 Oct 2026, 11:02",
                        showDeleteConfirmation = false,
                    ),
                    onEdit = { edited = true },
                )
            }
        }

        composeRule.onNodeWithText("Buy milk").assertIsDisplayed()
        composeRule.onNodeWithText("No description").assertIsDisplayed()
        composeRule.onNodeWithText("17 Oct 2026, 09:15").assertIsDisplayed()
        composeRule.onNodeWithTag(TaskDetailTags.EDIT).performClick()

        Assert.assertTrue(edited)
    }

    /**
     * When the load failed, then the failure message is shown.
     */
    @Test
    fun whenLoadFailed_thenFailureMessageIsShown() {
        composeRule.setContent {
            TaskAppTheme {
                TaskDetailScreen(state = TaskDetailUiState.LoadFailed)
            }
        }

        composeRule.onNodeWithText("Couldn't load this task").assertIsDisplayed()
    }
}