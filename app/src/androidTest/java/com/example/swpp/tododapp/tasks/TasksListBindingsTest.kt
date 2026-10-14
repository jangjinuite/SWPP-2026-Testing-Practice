package com.example.swpp.tododapp.tasks

import android.view.View
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.swpp.tododapp.data.Task
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TasksListBindingsTest {
    @Test
    fun setDueDateLabel_futureDate_showsCountdown() {
        // Given
        val today = LocalDate.now()
        val tasks = listOf(task(today.plusDays(6)))
        // When
        val states = bindOnMainThread(tasks)
        // Then
        assertStates(today, listOf("D-6" to View.VISIBLE), states)
    }

    @Test
    fun setDueDateLabel_today_showsDDay() {
        // Given
        val today = LocalDate.now()
        val tasks = listOf(task(today))
        // When
        val states = bindOnMainThread(tasks)
        // Then
        assertStates(today, listOf("D-Day" to View.VISIBLE), states)
    }

    @Test
    fun setDueDateLabel_pastDate_showsOverdueCount() {
        // Given
        val today = LocalDate.now()
        val tasks = listOf(task(today.minusDays(6)))
        // When
        val states = bindOnMainThread(tasks)
        // Then
        assertStates(today, listOf("D+6" to View.VISIBLE), states)
    }

    @Test
    fun setDueDateLabel_noDueDate_clearsAndHidesExistingLabel() {
        // Given
        val today = LocalDate.now()
        val tasks = listOf(task(null))
        // When
        val states = bindOnMainThread(tasks, "D-6", View.VISIBLE)
        // Then
        assertStates(today, listOf("" to View.GONE), states)
    }

    @Test
    fun setDueDateLabel_completedAcrossDateCategories_showsClear() {
        // Given
        val today = LocalDate.now()
        val tasks = listOf(null, today.minusDays(6), today, today.plusDays(6))
            .map { task(it, true) }
        // When
        val states = bindOnMainThread(tasks)
        // Then
        assertStates(today, listOf(
            "CLEAR" to View.VISIBLE, "CLEAR" to View.VISIBLE,
            "CLEAR" to View.VISIBLE, "CLEAR" to View.VISIBLE
        ), states)
    }

    @Test
    fun setDueDateLabel_todayBoundary_showsNeighboringLabels() {
        // Given
        val today = LocalDate.now()
        val tasks = listOf(today.minusDays(1), today, today.plusDays(1)).map { task(it) }
        // When
        val states = bindOnMainThread(tasks)
        // Then
        assertStates(today, listOf(
            "D+1" to View.VISIBLE, "D-Day" to View.VISIBLE, "D-1" to View.VISIBLE
        ), states)
    }

    @Test
    fun setDueDateLabel_monthLengthCountdowns_showsTotalDays() {
        // Given
        val today = LocalDate.now()
        val tasks = listOf(30L, 31L, 32L).map { task(today.plusDays(it)) }
        // When
        val states = bindOnMainThread(tasks)
        // Then
        assertStates(today, listOf(
            "D-30" to View.VISIBLE, "D-31" to View.VISIBLE, "D-32" to View.VISIBLE
        ), states)
    }

    @Test
    fun setDueDateLabel_yearLengthCountdowns_showsTotalDays() {
        // Given
        val today = LocalDate.now()
        val tasks = listOf(364L, 365L, 366L).map { task(today.plusDays(it)) }
        // When
        val states = bindOnMainThread(tasks)
        // Then
        assertStates(today, listOf(
            "D-364" to View.VISIBLE, "D-365" to View.VISIBLE, "D-366" to View.VISIBLE
        ), states)
    }

    @Test
    fun setDueDateLabel_datedTaskCompletionToggled_restoresLabelsOnSameView() {
        // Given
        val today = LocalDate.now()
        val tasks = listOf(today.minusDays(1), today, today.plusDays(1)).flatMap { date ->
            val activeTask = task(date)
            listOf(activeTask, activeTask.copy(isCompleted = true), activeTask)
        }
        // When
        val states = bindOnMainThread(tasks)
        // Then
        assertStates(today, listOf(
            "D+1" to View.VISIBLE, "CLEAR" to View.VISIBLE, "D+1" to View.VISIBLE,
            "D-Day" to View.VISIBLE, "CLEAR" to View.VISIBLE, "D-Day" to View.VISIBLE,
            "D-1" to View.VISIBLE, "CLEAR" to View.VISIBLE, "D-1" to View.VISIBLE
        ), states)
    }

    @Test
    fun setDueDateLabel_undatedTaskCompletionToggled_clearsStaleTextOnSameView() {
        // Given
        val today = LocalDate.now()
        val activeTask = task(null)
        val tasks = listOf(activeTask, activeTask.copy(isCompleted = true), activeTask)
        // When
        val states = bindOnMainThread(tasks, "D-6", View.VISIBLE)
        // Then
        assertStates(today, listOf(
            "" to View.GONE, "CLEAR" to View.VISIBLE, "" to View.GONE
        ), states)
    }

    @Test
    fun setDueDateLabel_viewReboundAcrossDueDates_replacesTextAndVisibility() {
        // Given
        val today = LocalDate.now()
        val tasks = listOf(task(today.plusDays(1)), task(null), task(today.minusDays(1)))
        // When
        val states = bindOnMainThread(tasks)
        // Then
        assertStates(today, listOf(
            "D-1" to View.VISIBLE, "" to View.GONE, "D+1" to View.VISIBLE
        ), states)
    }

    private fun task(dueDate: LocalDate?, isCompleted: Boolean = false) = Task(
        title = "Task", description = "", id = "task-1",
        dueDate = dueDate, isCompleted = isCompleted
    )

    private fun bindOnMainThread(
        tasks: List<Task>,
        initialText: String = "",
        initialVisibility: Int = View.GONE
    ): List<Pair<String, Int>> {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val states = mutableListOf<Pair<String, Int>>()
        instrumentation.runOnMainSync {
            val textView = TextView(instrumentation.targetContext).apply {
                text = initialText
                visibility = initialVisibility
            }
            tasks.forEach { task ->
                setDueDateLabel(textView, task)
                states.add(textView.text.toString() to textView.visibility)
            }
        }
        return states
    }

    private fun assertStates(
        today: LocalDate,
        expected: List<Pair<String, Int>>,
        actual: List<Pair<String, Int>>
    ) {
        // Discard a midnight-crossing run before evaluating its date-dependent oracle.
        assumeTrue("Local date changed during the test; rerun with a newly captured date", today == LocalDate.now())
        try {
            assertEquals(expected, actual)
        } finally {
            assumeTrue("Local date changed before assertions finished; rerun the test", today == LocalDate.now())
        }
    }
}
