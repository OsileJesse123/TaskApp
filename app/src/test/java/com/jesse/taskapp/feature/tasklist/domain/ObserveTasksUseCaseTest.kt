package com.jesse.taskapp.feature.tasklist.domain

import com.jesse.taskapp.FakeTaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
        useCase("  milk  ", createdOnUtcMillis = null, zone = ZoneId.of("UTC")).first()

        assertEquals("milk", repository.observedQueries.single())
        assertNull(repository.observedFrom.single())
        assertNull(repository.observedTo.single())
    }

    /**
     * When no creation day is selected, then both bounds are null.
     */
    @Test
    fun whenNoDateIsSelected_thenBoundsAreOpen() = runTest {
        useCase("", createdOnUtcMillis = null, zone = ZoneId.of("UTC")).first()

        assertEquals("", repository.observedQueries.single())
        assertNull(repository.observedFrom.single())
        assertNull(repository.observedTo.single())
    }

    /**
     * When a creation day is selected, then the repository receives that local day's bounds.
     */
    @Test
    fun whenDateIsSelected_thenRepositoryReceivesThatLocalDay() = runTest {
        val zone = ZoneId.of("UTC")
        val picked = LocalDate.of(2026, 10, 17).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        useCase("milk", picked, zone).first()

        val expected = creationDayWindow(picked, zone)
        assertEquals("milk", repository.observedQueries.single())
        assertEquals(expected.startMillisInclusive, repository.observedFrom.single())
        assertEquals(expected.endMillisExclusive, repository.observedTo.single())
    }
}
