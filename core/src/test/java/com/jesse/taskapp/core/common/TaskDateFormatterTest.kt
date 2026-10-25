package com.jesse.taskapp.core.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

/**
 * Tests for [TaskDateFormatter].
 */
class TaskDateFormatterTest {
    private val formatter = TaskDateFormatter()

    /**
     * When an instant is formatted in UTC, then the clock time is that UTC time.
     */
    @Test
    fun whenInstantIsFormattedInUtc_thenClockTimeIsShown() {
        val millis = LocalDateTime.of(2026, 10, 17, 9, 15)
            .atZone(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        val text = formatter.formatDateTime(millis, ZoneId.of("UTC"), Locale.US)

        assertEquals("17 Oct 2026, 09:15", text)
    }

    /**
     * When a picker day is formatted, then the calendar date is the UTC date, not the previous local date.
     */
    @Test
    fun whenPickerDayIsFormatted_thenUtcCalendarDateIsShown() {
        val picked = LocalDate.of(2026, 10, 17).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val text = formatter.formatPickerDate(picked, Locale.US)

        assertEquals("17 Oct 2026", text)
    }

    /**
     * When the range is one day, then a single picker date is shown.
     */
    @Test
    fun whenRangeIsOneDay_thenOnePickerDateIsShown() {
        val picked = LocalDate.of(2026, 10, 17).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val text = formatter.formatPickerDateRange(picked, picked, Locale.US)

        assertEquals("17 Oct 2026", text)
    }

    /**
     * When the range spans two days, then both picker dates are shown.
     */
    @Test
    fun whenRangeSpansDays_thenBothPickerDatesAreShown() {
        val start = LocalDate.of(2026, 10, 17).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 10, 19).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val text = formatter.formatPickerDateRange(start, end, Locale.US)

        assertEquals("17 Oct 2026 – 19 Oct 2026", text)
    }
}
