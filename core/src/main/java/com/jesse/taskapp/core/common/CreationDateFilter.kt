package com.jesse.taskapp.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

/**
 * Inclusive lower and exclusive upper creation-time bounds for repository queries.
 *
 * @property fromMillisInclusive first instant included in the filter, or null for no lower bound.
 * @property toMillisExclusive first instant after the filter window, or null for no upper bound.
 */
data class CreationDateBounds(
    val fromMillisInclusive: Long?,
    val toMillisExclusive: Long?,
)

/**
 * Parses optional ISO creation dates and maps them to epoch millis for repository filters.
 *
 * Calendar days are interpreted in [ZoneId.systemDefault] unless a caller passes another [ZoneId]
 * to [resolve]. That matches how creation timestamps are filtered elsewhere in the app.
 */
class CreationDateFilter @Inject constructor(
    private val timeProvider: TimeProvider,
) {
    /**
     * Resolves optional [createdFrom] and [createdTo] strings into repository millis bounds.
     *
     * Dates use [ISO_DATE_PATTERN] and are interpreted as calendar days in [zone] (device default
     * when omitted). Blank strings are treated as absent. [createdTo] is inclusive at the API level;
     * this method converts it to an exclusive upper bound at the start of the next local day.
     * Validation fails for invalid formats, inverted ranges, or future dates.
     *
     * @param createdFrom optional inclusive start date.
     * @param createdTo optional inclusive end date.
     * @param zone timezone that defines each yyyy-MM-dd calendar day.
     * @return millis bounds for task repository queries.
     * @throws InvalidCreationDateException when dates are invalid or out of range.
     */
    fun resolve(
        createdFrom: String?,
        createdTo: String?,
        zone: ZoneId = ZoneId.systemDefault(),
    ): CreationDateBounds {
        val fromText = createdFrom?.trim()?.takeIf { it.isNotEmpty() }
        val toText = createdTo?.trim()?.takeIf { it.isNotEmpty() }

        val fromDate = fromText?.let { parseIsoDate(it, "createdFrom") }
        val toDate = toText?.let { parseIsoDate(it, "createdTo") }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw InvalidCreationDateException(
                "createdTo ($toText) must be on or after createdFrom ($fromText).",
            )
        }

        val today = Instant.ofEpochMilli(timeProvider.nowMillis()).atZone(zone).toLocalDate()
        fromDate?.let { date ->
            if (date.isAfter(today)) {
                throw InvalidCreationDateException(
                    "createdFrom ($fromText) cannot be in the future. Today is $today.",
                )
            }
        }
        toDate?.let { date ->
            if (date.isAfter(today)) {
                throw InvalidCreationDateException(
                    "createdTo ($toText) cannot be in the future. Today is $today.",
                )
            }
        }

        val fromMillis = fromDate?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
        val toMillis = toDate?.plusDays(1)?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()

        return CreationDateBounds(
            fromMillisInclusive = fromMillis,
            toMillisExclusive = toMillis,
        )
    }

    private fun parseIsoDate(value: String, parameterName: String): LocalDate {
        return try {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: DateTimeParseException) {
            throw InvalidCreationDateException(
                "$parameterName must be a calendar date in $ISO_DATE_PATTERN format (for example 2026-10-17).",
            )
        }
    }

    private companion object {
        const val ISO_DATE_PATTERN = "yyyy-MM-dd"
    }
}
