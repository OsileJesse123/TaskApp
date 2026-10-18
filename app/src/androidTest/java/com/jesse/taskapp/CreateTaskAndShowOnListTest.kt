package com.jesse.taskapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.jesse.taskapp.feature.taskform.presentation.TaskFormTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Creates a task through the real navigation graph and checks that the list shows it.
 *
 * The database is the in-memory replacement from [com.jesse.taskapp.core.di.TestDatabaseModule].
 */
@HiltAndroidTest
class CreateTaskAndShowOnListTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    /**
     * Injects the test so Hilt installs the in-memory database before the activity is used.
     */
    @Before
    fun setUp() {
        hiltRule.inject()
    }

    /**
     * When the user saves a task from the list, then that title is shown on the list.
     */
    @Test
    fun whenUserSavesATask_thenListShowsIt() {
        // The empty state is the signal that the database stream has emitted.
        composeRule.onNodeWithText("No tasks yet").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Create task").performClick()
        composeRule.onNodeWithTag(TaskFormTags.TITLE).performTextInput("Buy milk")
        composeRule.onNodeWithTag(TaskFormTags.SAVE).performClick()

        composeRule.onNodeWithText("Buy milk").assertIsDisplayed()
    }
}
