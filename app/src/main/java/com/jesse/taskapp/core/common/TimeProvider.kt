package com.jesse.taskapp.core.common

import javax.inject.Inject

/**
 * Supplies the current time as epoch millis.
 *
 * Use cases take this instead of calling [System.currentTimeMillis] so tests can freeze time
 * and assert the timestamps written with a task.
 */
fun interface TimeProvider {
    /**
     * Returns the current instant as epoch millis.
     *
     * @return epoch millis to store as a created or edited timestamp.
     */
    fun nowMillis(): Long
}

/**
 * [TimeProvider] backed by the device clock.
 */
class SystemTimeProvider @Inject constructor() : TimeProvider {
    /**
     * Returns [System.currentTimeMillis].
     *
     * @return the device clock in epoch millis.
     */
    override fun nowMillis(): Long {
        // The platform clock is the source of truth for timestamps stored on the device.
        return System.currentTimeMillis()
    }
}
