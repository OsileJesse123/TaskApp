package com.jesse.taskapp

import com.jesse.taskapp.core.common.TimeProvider

/**
 * Clock whose time is chosen by the test.
 *
 * @property nowMillis instant returned until the test changes it.
 */
class FakeTimeProvider(
    var nowMillis: Long = 1_700_000_000_000,
) : TimeProvider {
    /**
     * Returns [nowMillis].
     *
     * @return the instant configured by the test.
     */
    override fun nowMillis(): Long = nowMillis
}
