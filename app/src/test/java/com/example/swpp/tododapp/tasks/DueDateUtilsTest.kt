package com.example.swpp.tododapp.tasks

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DueDateUtilsTest {
    @Test
    fun dueDateLabel_futureDate_returnsCountdown() {
        // Given
        val today = LocalDate.of(2026, 10, 14)
        val dueDate = LocalDate.of(2026, 10, 20)
        // When
        val result = dueDateLabel(dueDate, today, false)
        // Then
        assertEquals("D-6", result)
    }

    @Test
    fun dueDateLabel_today_returnsDDay() {
        // Given
        val today = LocalDate.of(2026, 10, 14)
        // When
        val result = dueDateLabel(today, today, false)
        // Then
        assertEquals("D-Day", result)
    }

    @Test
    fun dueDateLabel_pastDate_returnsOverdueCount() {
        // Given
        val today = LocalDate.of(2026, 10, 14)
        val dueDate = LocalDate.of(2026, 10, 8)
        // When
        val result = dueDateLabel(dueDate, today, false)
        // Then
        assertEquals("D+6", result)
    }

    @Test
    fun dueDateLabel_noDueDate_returnsEmptyString() {
        // Given
        val today = LocalDate.of(2026, 10, 14)
        // When
        val result = dueDateLabel(null, today, false)
        // Then
        assertEquals("", result)
    }

    @Test
    fun dueDateLabel_completedAcrossDateCategories_returnsClear() {
        // Given
        val today = LocalDate.of(2026, 10, 14)
        val dates = listOf(null, LocalDate.of(2026, 10, 8), today, LocalDate.of(2026, 10, 20))
        // When
        val results = dates.map { dueDateLabel(it, today, true) }
        // Then
        assertEquals(listOf("CLEAR", "CLEAR", "CLEAR", "CLEAR"), results)
    }

    @Test
    fun dueDateLabel_todayBoundary_returnsNeighboringLabels() {
        // Given
        val today = LocalDate.of(2026, 10, 14)
        val dates = listOf(LocalDate.of(2026, 10, 13), today, LocalDate.of(2026, 10, 15))
        // When
        val results = dates.map { dueDateLabel(it, today, false) }
        // Then
        assertEquals(listOf("D+1", "D-Day", "D-1"), results)
    }

    @Test
    fun dueDateLabel_monthTransition_countsAllDays() {
        // Given
        val today = LocalDate.of(2026, 1, 30)
        val dates = listOf(LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 2))
        // When
        val results = dates.map { dueDateLabel(it, today, false) }
        // Then
        assertEquals(listOf("D-1", "D-2", "D-3"), results)
    }

    @Test
    fun dueDateLabel_fullMonthAndAdjacentDates_countsAllDaysInBothDirections() {
        // Given
        val today = LocalDate.of(2026, 1, 15)
        val dates = listOf(LocalDate.of(2026, 2, 14), LocalDate.of(2026, 2, 15), LocalDate.of(2026, 2, 16))
        // When
        val forward = dates.map { dueDateLabel(it, today, false) }
        val backward = dates.map { dueDateLabel(today, it, false) }
        // Then
        assertEquals(listOf("D-30", "D-31", "D-32"), forward)
        assertEquals(listOf("D+30", "D+31", "D+32"), backward)
    }

    @Test
    fun dueDateLabel_yearTransition_countsAllDaysInBothDirections() {
        // Given
        val today = LocalDate.of(2026, 12, 30)
        val dates = listOf(LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2))
        // When
        val forward = dates.map { dueDateLabel(it, today, false) }
        val backward = dates.map { dueDateLabel(today, it, false) }
        // Then
        assertEquals(listOf("D-1", "D-2", "D-3"), forward)
        assertEquals(listOf("D+1", "D+2", "D+3"), backward)
    }

    @Test
    fun dueDateLabel_fullNonLeapYearAndAdjacentDates_countsAllDays() {
        // Given
        val today = LocalDate.of(2025, 1, 1)
        val dates = listOf(LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2))
        // When
        val results = dates.map { dueDateLabel(it, today, false) }
        // Then
        assertEquals(listOf("D-364", "D-365", "D-366"), results)
    }

    @Test
    fun dueDateLabel_leapDayBoundary_countsAllDaysInBothDirections() {
        // Given
        val today = LocalDate.of(2028, 2, 27)
        val dates = listOf(LocalDate.of(2028, 2, 28), LocalDate.of(2028, 2, 29), LocalDate.of(2028, 3, 1))
        // When
        val forward = dates.map { dueDateLabel(it, today, false) }
        val backward = dates.map { dueDateLabel(today, it, false) }
        // Then
        assertEquals(listOf("D-1", "D-2", "D-3"), forward)
        assertEquals(listOf("D+1", "D+2", "D+3"), backward)
    }

    @Test
    fun dueDateLabel_nonLeapFebruaryTransition_countsAllDays() {
        // Given
        val today = LocalDate.of(2026, 2, 27)
        val dates = listOf(LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2))
        // When
        val results = dates.map { dueDateLabel(it, today, false) }
        // Then
        assertEquals(listOf("D-1", "D-2", "D-3"), results)
    }

    @Test
    fun dueDateLabel_completionToggled_restoresOriginalLabels() {
        // Given
        val today = LocalDate.of(2026, 10, 14)
        val dates = listOf(null, LocalDate.of(2026, 10, 13), today, LocalDate.of(2026, 10, 15))
        // When
        val results = dates.map { date ->
            listOf(false, true, false).map { completed -> dueDateLabel(date, today, completed) }
        }
        // Then
        assertEquals(
            listOf(
                listOf("", "CLEAR", ""),
                listOf("D+1", "CLEAR", "D+1"),
                listOf("D-Day", "CLEAR", "D-Day"),
                listOf("D-1", "CLEAR", "D-1")
            ),
            results
        )
    }
}
