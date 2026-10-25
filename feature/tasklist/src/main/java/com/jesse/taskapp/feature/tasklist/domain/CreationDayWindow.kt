package com.jesse.taskapp.feature.tasklist.domain

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Inclusive start and exclusive end of one local calendar day, in epoch millis.
 *
 * @property startMillisInclusive first instant of the day in the chosen zone.
 * @property endMillisExclusive first instant of the next day in the chosen zone.
 */
data class CreationDayWindow(
    val startMillisInclusive: Long,
    val endMillisExclusive: Long,
)

/**
 * Turns the calendar date chosen in the date picker into a local-day window.
 *
 * Material 3 reports the selected day as UTC midnight. The year-month-day is read in UTC so the
 * picker day is preserved, then the window is that calendar date in [zone]. The DAO compares
 * those millis and does not call SQLite date functions.
 *
 * @param selectedDateUtcMillis UTC midnight of the day chosen in the picker.
 * @param zone zone whose midnight bounds should be used.
 * @return the local-day window for that calendar date.
 */
fun creationDayWindow(selectedDateUtcMillis: Long, zone: ZoneId): CreationDayWindow {
    // Read the picker value in UTC. Interpreting it in a zone behind UTC would select the previous day.
    val date = Instant.ofEpochMilli(selectedDateUtcMillis).atZone(ZoneOffset.UTC).toLocalDate()
    val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return CreationDayWindow(start, end)
}

/**
 * Turns the start and end days from the date-range picker into one local millis window.
 *
 * Each bound is a UTC midnight from Material 3. The window starts at local midnight of the first
 * day and ends at local midnight of the day after the last day, so the last day is included.
 *
 * @param startDateUtcMillis UTC midnight of the first selected day.
 * @param endDateUtcMillis UTC midnight of the last selected day.
 * @param zone zone whose midnight bounds should be used.
 * @return the inclusive-start, exclusive-end window covering every local day in the range.
 */
fun creationDateRangeWindow(
    startDateUtcMillis: Long,
    endDateUtcMillis: Long,
    zone: ZoneId,
): CreationDayWindow {
    val start = creationDayWindow(startDateUtcMillis, zone)
    val end = creationDayWindow(endDateUtcMillis, zone)
    return CreationDayWindow(
        startMillisInclusive = start.startMillisInclusive,
        endMillisExclusive = end.endMillisExclusive,
    )
}
