package com.jesse.taskapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point that starts Hilt.
 *
 * Hilt generates the singleton component from this class. Feature code never constructs the
 * database or repositories itself.
 */
@HiltAndroidApp
class TaskApplication : Application()
