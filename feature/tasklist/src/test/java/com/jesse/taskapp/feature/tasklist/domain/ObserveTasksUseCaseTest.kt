package com.jesse.taskapp.feature.tasklist.domain

import com.jesse.taskapp.FakeTaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Tests for the arguments [ObserveTasksUseCase] sends to the repository.
 */
class ObserveTasksUseCaseTest {
    private val repository = FakeTaskRepository()
    private val useCase = ObserveTasksUseCase(repository)

    /**
     * When the search text has surrounding spaces, then the repository receives the trimmed text.
     */
    @Test
    fun whenQueryHasSpaces_thenRepositoryReceivesTrimmedText() = runTest {
        useCase(
            "  milk  ",
            createdFromUtcMillis = null,
            createdToUtcMillis = null,
            zone = ZoneId.of("UTC"),
        ).first()

        assertEquals("milk", repository.observedQueries.single())
        assertNull(repository.observedFrom.single())
        assertNull(repository.observedTo.single())
    }

    /**
     * When no creation range is selected, then both bounds are null.
     */
    @Test
    fun whenNoDateIsSelected_thenBoundsAreOpen() = runTest {
        useCase("", createdFromUtcMillis = null, createdToUtcMillis = null, zone = ZoneId.of("UTC")).first()

        assertEquals("", repository.observedQueries.single())
        assertNull(repository.observedFrom.single())
        assertNull(repository.observedTo.single())
    }

    /**
     * When only one bound is set, then the date filter is ignored.
     */
    @Test
    fun whenOnlyOneBoundIsSet_thenBoundsAreOpen() = runTest {
        val picked = LocalDate.of(2026, 10, 17).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        useCase("milk", createdFromUtcMillis = picked, createdToUtcMillis = null, zone = ZoneId.of("UTC")).first()

        assertNull(repository.observedFrom.single())
        assertNull(repository.observedTo.single())
    }

    /**
     * When a creation range is selected, then the repository receives that local window.
     */
    @Test
    fun whenDateRangeIsSelected_thenRepositoryReceivesThatLocalWindow() = runTest {
        val zone = ZoneId.of("UTC")
        val start = LocalDate.of(2026, 10, 17).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 10, 19).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        useCase("milk", start, end, zone).first()

        val expected = creationDateRangeWindow(start, end, zone)
        assertEquals("milk", repository.observedQueries.single())
        Assert.assertEquals(expected.startMillisInclusive, repository.observedFrom.single())
        Assert.assertEquals(expected.endMillisExclusive, repository.observedTo.single())
    }
}
