package com.jesse.taskapp.feature.tasklist.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.jesse.taskapp.ui.theme.TaskAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Instrumentation tests for [TaskListScreen] with state passed in directly.
 */
class TaskListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    /**
     * When there are no tasks, then the empty message is shown.
     */
    @Test
    fun whenThereAreNoTasks_thenEmptyMessageIsShown() {
        composeRule.setContent {
            TaskAppTheme {
                TaskListScreen(state = TaskListUiState(content = TaskListContent.Empty))
            }
        }

        composeRule.onNodeWithText("No tasks yet").assertIsDisplayed()
    }

    /**
     * When tasks are present, then their titles are shown.
     */
    @Test
    fun whenTasksArePresent_thenTitlesAreShown() {
        composeRule.setContent {
            TaskAppTheme {
                TaskListScreen(state = contentState(selectionMode = false))
            }
        }

        composeRule.onNodeWithText("Buy milk").assertIsDisplayed()
        composeRule.onNodeWithText("Write notes").assertIsDisplayed()
    }

    /**
     * When the user long-presses a row, then that id is offered for selection.
     */
    @Test
    fun whenRowIsLongPressed_thenSelectionStartsForThatId() {
        var started: Long? = null
        composeRule.setContent {
            TaskAppTheme {
                TaskListScreen(
                    state = contentState(selectionMode = false),
                    onStartSelection = { started = it },
                )
            }
        }

        composeRule.onNodeWithText("Buy milk").performTouchInput { longClick() }

        assertEquals(1L, started)
    }

    /**
     * When selection mode is on, then a tap toggles that row and delete is available.
     */
    @Test
    fun whenSelectionIsActive_thenTapTogglesAndDeleteIsAvailable() {
        var toggled: Long? = null
        var deleteRequested = false
        composeRule.setContent {
            TaskAppTheme {
                TaskListScreen(
                    state = contentState(selectionMode = true),
                    onToggleSelected = { toggled = it },
                    onDeleteSelection = { deleteRequested = true },
                )
            }
        }

        composeRule.onNodeWithText("1 selected").assertIsDisplayed()
        composeRule.onNodeWithText("Selected").assertIsDisplayed()
        composeRule.onNodeWithText("Buy milk").performClick()
        composeRule.onNodeWithTag(TaskListTags.DELETE).performClick()

        assertEquals(1L, toggled)
        assertTrue(deleteRequested)
    }

    /**
     * When the confirm dialog is open, then confirming reports that choice.
     */
    @Test
    fun whenDeleteIsConfirmed_thenConfirmCallbackRuns() {
        var confirmed = false
        composeRule.setContent {
            TaskAppTheme {
                TaskListScreen(
                    state = contentState(selectionMode = true).copy(showDeleteConfirmation = true),
                    onConfirmDelete = { confirmed = true },
                )
            }
        }

        composeRule.onNodeWithText("Delete tasks?").assertIsDisplayed()
        composeRule.onNodeWithTag(TaskListTags.CONFIRM_DELETE).performClick()

        assertTrue(confirmed)
    }

    /**
     * When the search matches nothing, then the no-results message is shown.
     */
    @Test
    fun whenSearchMatchesNothing_thenNoResultsMessageIsShown() {
        composeRule.setContent {
            TaskAppTheme {
                TaskListScreen(
                    state = TaskListUiState(query = "milk", content = TaskListContent.NoResults),
                )
            }
        }

        composeRule.onNodeWithText("No matching tasks").assertIsDisplayed()
    }

    /**
     * Builds a two-row list state.
     *
     * @param selectionMode true to select the first row.
     * @return state for a screen test.
     */
    private fun contentState(selectionMode: Boolean): TaskListUiState {
        val tasks = listOf(
            TaskListItemUi(1, "Buy milk", "Oat", "17 Oct 2026, 09:15", selected = selectionMode),
            TaskListItemUi(2, "Write notes", "", "16 Oct 2026, 18:40", selected = false),
        )
        return TaskListUiState(
            selectionMode = selectionMode,
            selectedCount = if (selectionMode) 1 else 0,
            content = TaskListContent.Tasks(tasks),
        )
    }
}
