package com.jesse.taskapp.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
/**
 * Tests for [CreationDateFilter].
 */
class CreationDateFilterTest {
    private val zone = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 11, 5)

    private fun filter(nowMillis: Long = today.atStartOfDay(zone).toInstant().toEpochMilli()): CreationDateFilter {
        return CreationDateFilter(timeProvider = TimeProvider { nowMillis })
    }

    /**
     * When both bounds are absent, then no millis filter is applied.
     */
    @Test
    fun whenBothDatesAreNull_thenBoundsAreOpen() {
        val bounds = filter().resolve(createdFrom = null, createdTo = null, zone = zone)

        assertNull(bounds.fromMillisInclusive)
        assertNull(bounds.toMillisExclusive)
    }

    /**
     * When both bounds are blank, then they are treated as absent.
     */
    @Test
    fun whenBothDatesAreBlank_thenBoundsAreOpen() {
        val bounds = filter().resolve(createdFrom = "  ", createdTo = "", zone = zone)

        assertNull(bounds.fromMillisInclusive)
        assertNull(bounds.toMillisExclusive)
    }

    /**
     * When a valid range is given, then inclusive and exclusive millis match local days.
     */
    @Test
    fun whenValidRangeIsGiven_thenLocalDayWindowIsReturned() {
        val bounds = filter().resolve(
            createdFrom = "2026-10-17",
            createdTo = "2026-10-19",
            zone = zone,
        )

        val expectedStart = LocalDate.of(2026, 10, 17)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val expectedEnd = LocalDate.of(2026, 10, 20)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedStart, bounds.fromMillisInclusive)
        assertEquals(expectedEnd, bounds.toMillisExclusive)
    }

    /**
     * When only createdFrom is set, then only the lower bound is applied.
     */
    @Test
    fun whenOnlyCreatedFromIsSet_thenLowerBoundIsApplied() {
        val bounds = filter().resolve(createdFrom = "2026-10-17", createdTo = null, zone = zone)

        val expectedStart = LocalDate.of(2026, 10, 17)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedStart, bounds.fromMillisInclusive)
        assertNull(bounds.toMillisExclusive)
    }

    /**
     * When only createdTo is set, then only the upper bound is applied.
     */
    @Test
    fun whenOnlyCreatedToIsSet_thenUpperBoundIsApplied() {
        val bounds = filter().resolve(createdFrom = null, createdTo = "2026-10-19", zone = zone)

        val expectedEnd = LocalDate.of(2026, 10, 20)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        assertNull(bounds.fromMillisInclusive)
        assertEquals(expectedEnd, bounds.toMillisExclusive)
    }

    /**
     * When createdFrom is after createdTo, then validation fails.
     */
    @Test(expected = InvalidCreationDateException::class)
    fun whenFromIsAfterTo_thenValidationFails() {
        filter().resolve(
            createdFrom = "2026-11-05",
            createdTo = "2026-10-13",
            zone = zone,
        )
    }

    /**
     * When createdFrom is in the future, then validation fails.
     */
    @Test(expected = InvalidCreationDateException::class)
    fun whenCreatedFromIsInTheFuture_thenValidationFails() {
        filter().resolve(createdFrom = "2026-11-06", createdTo = null, zone = zone)
    }

    /**
     * When createdTo is in the future, then validation fails.
     */
    @Test(expected = InvalidCreationDateException::class)
    fun whenCreatedToIsInTheFuture_thenValidationFails() {
        filter().resolve(createdFrom = null, createdTo = "2026-11-06", zone = zone)
    }

    /**
     * When the date string is not ISO format, then validation fails.
     */
    @Test(expected = InvalidCreationDateException::class)
    fun whenDateFormatIsInvalid_thenValidationFails() {
        filter().resolve(createdFrom = "05-10-2026", createdTo = null, zone = zone)
    }

    /**
     * When the calendar date does not exist, then validation fails.
     */
    @Test(expected = InvalidCreationDateException::class)
    fun whenCalendarDateIsInvalid_thenValidationFails() {
        filter().resolve(createdFrom = "2026-13-01", createdTo = null, zone = zone)
    }
}
