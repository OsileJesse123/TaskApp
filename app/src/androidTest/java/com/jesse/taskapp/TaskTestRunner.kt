package com.jesse.taskapp

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Starts instrumentation tests with [HiltTestApplication] so Hilt can inject test modules.
 */
class TaskTestRunner : AndroidJUnitRunner() {
    /**
     * Replaces the app class with the Hilt test application.
     *
     * @param cl test class loader.
     * @param className application class requested by the manifest. Replaced below.
     * @param context instrumentation context.
     * @return the Hilt test application.
     */
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application {
        // Every instrumentation test in this process uses Hilt, including tests that never inject.
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
