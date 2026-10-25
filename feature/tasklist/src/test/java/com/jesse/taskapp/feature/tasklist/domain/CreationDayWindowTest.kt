package com.jesse.taskapp.feature.tasklist.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Tests for turning a date-picker day into a local creation window.
 */
class CreationDayWindowTest {
    /**
     * When the picker selects 17 Oct 2026, then the window is that calendar date in the device zone,
     * not the UTC instant shifted into the previous evening.
     */
    @Test
    fun whenPickerSelectsADay_thenWindowUsesThatCalendarDateInTheZone() {
        // The Material picker reports this day as UTC midnight.
        val picked = LocalDate.of(2026, 10, 17)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val zone = ZoneId.of("America/New_York")

        val window = creationDayWindow(picked, zone)

        // New York midnight, not UTC midnight. UTC midnight would still be the previous evening there.
        val expectedStart = LocalDate.of(2026, 10, 17).atStartOfDay(zone).toInstant().toEpochMilli()
        val expectedEnd = LocalDate.of(2026, 10, 18).atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expectedStart, window.startMillisInclusive)
        assertEquals(expectedEnd, window.endMillisExclusive)
    }

    /**
     * When the picker selects 17 Oct through 19 Oct 2026, then the window includes local 19 Oct.
     */
    @Test
    fun whenPickerSelectsARange_thenWindowIncludesTheLastLocalDay() {
        val startPicked = LocalDate.of(2026, 10, 17)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val endPicked = LocalDate.of(2026, 10, 19)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val zone = ZoneId.of("America/New_York")

        val window = creationDateRangeWindow(startPicked, endPicked, zone)

        val expectedStart = LocalDate.of(2026, 10, 17).atStartOfDay(zone).toInstant().toEpochMilli()
        val expectedEnd = LocalDate.of(2026, 10, 20).atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expectedStart, window.startMillisInclusive)
        assertEquals(expectedEnd, window.endMillisExclusive)
    }

    /**
     * When the start and end days are the same, then the window is that one local day.
     */
    @Test
    fun whenRangeIsOneDay_thenWindowMatchesThatLocalDay() {
        val picked = LocalDate.of(2026, 10, 17)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val zone = ZoneId.of("UTC")

        val window = creationDateRangeWindow(picked, picked, zone)

        assertEquals(creationDayWindow(picked, zone), window)
    }
}
