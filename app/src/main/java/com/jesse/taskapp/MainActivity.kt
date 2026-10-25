package com.jesse.taskapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jesse.taskapp.navigation.TaskNavHost
import com.jesse.taskapp.ui.TaskAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity that hosts the Compose navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Shows the task navigation graph.
     *
     * @param savedInstanceState state restored by the platform. Compose navigation keeps its own back stack.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskAppTheme {
                TaskNavHost()
            }
        }
    }
}
