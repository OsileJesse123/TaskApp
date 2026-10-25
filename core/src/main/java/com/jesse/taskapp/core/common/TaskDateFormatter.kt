package com.jesse.taskapp.core.common

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Formats task instants and date-picker days for display.
 */
class TaskDateFormatter @Inject constructor() {
    /**
     * Formats an instant in [zone].
     *
     * @param epochMillis created or edited instant.
     * @param zone zone used to render the instant. Defaults to the device zone.
     * @param locale locale used for the month name. Defaults to the device locale.
     * @return a short local date and time, for example "17 Oct 2026, 16:05".
     */
    fun formatDateTime(
        epochMillis: Long,
        zone: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
    ): String {
        // Build the formatter per call so a test can pin the locale without changing the device default.
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", locale)
        val zoned = Instant.ofEpochMilli(epochMillis).atZone(zone)
        return formatter.format(zoned)
    }

    /**
     * Formats the calendar day chosen in the Material date picker.
     *
     * The picker reports UTC midnight for the selected calendar date. That value is read in UTC
     * so the day does not shift when the device is behind UTC.
     *
     * @param selectedDateUtcMillis UTC midnight of the selected calendar date.
     * @param locale locale used for the month name. Defaults to the device locale.
     * @return the calendar date without a time, for example "17 Oct 2026".
     */
    fun formatPickerDate(
        selectedDateUtcMillis: Long,
        locale: Locale = Locale.getDefault(),
    ): String {
        // UTC keeps the picker day stable. Converting the same millis through a negative offset
        // would show the previous local date.
        val date = Instant.ofEpochMilli(selectedDateUtcMillis).atZone(ZoneOffset.UTC).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
        return formatter.format(date)
    }

    /**
     * Formats a creation-date range from the date-range picker.
     *
     * A single selected day is shown as one date. A span is shown as two picker days.
     *
     * @param startDateUtcMillis UTC midnight of the first selected day.
     * @param endDateUtcMillis UTC midnight of the last selected day.
     * @param locale locale used for the month name. Defaults to the device locale.
     * @return one date, or "start – end".
     */
    fun formatPickerDateRange(
        startDateUtcMillis: Long,
        endDateUtcMillis: Long,
        locale: Locale = Locale.getDefault(),
    ): String {
        val start = formatPickerDate(startDateUtcMillis, locale)
        if (startDateUtcMillis == endDateUtcMillis) {
            return start
        }
        val end = formatPickerDate(endDateUtcMillis, locale)
        return "$start – $end"
    }
}
