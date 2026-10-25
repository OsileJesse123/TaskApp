package com.jesse.taskapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Replaces [Dispatchers.Main] for ViewModel tests.
 *
 * Pass [dispatcher] to `runTest` so [kotlinx.coroutines.test.advanceTimeBy] moves the same
 * clock that ViewModel coroutines and search debounce use.
 *
 * @param dispatcher dispatcher installed as Main.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    /**
     * Installs [dispatcher] as Main before the test body.
     *
     * @param description current test description. Unused.
     */
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    /**
     * Restores the original Main dispatcher after the test.
     *
     * @param description current test description. Unused.
     */
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
