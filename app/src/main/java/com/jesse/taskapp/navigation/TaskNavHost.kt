package com.jesse.taskapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jesse.taskapp.feature.taskdetail.presentation.TaskDetailRoute
import com.jesse.taskapp.feature.taskform.presentation.TaskFormRoute
import com.jesse.taskapp.feature.tasklist.presentation.TaskListRoute

/**
 * Navigation graph for the task screens.
 *
 * This is the app shell, so it may depend on feature routes. Feature packages do not depend on
 * each other or on this graph. Each destination passes an id, then the next screen loads the task.
 *
 * @param navController controller for this graph. A new one is created when the host is first composed.
 */
@Composable
fun TaskNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = TaskListDestination,
    ) {
        composable<TaskListDestination> {
            TaskListRoute(
                onOpenTask = { taskId ->
                    navController.navigate(TaskDetailDestination(taskId))
                },
                onCreateTask = {
                    navController.navigate(TaskCreateDestination)
                },
            )
        }
        composable<TaskDetailDestination> { entry ->
            val destination = entry.toRoute<TaskDetailDestination>()
            TaskDetailRoute(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(TaskEditDestination(destination.taskId)) },
                onDeleted = { navController.popBackStack() },
            )
        }
        composable<TaskCreateDestination> {
            TaskFormRoute(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable<TaskEditDestination> {
            TaskFormRoute(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}
