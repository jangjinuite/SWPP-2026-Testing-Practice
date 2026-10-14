# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Bind the current task's due-date label to its TextView using today's local calendar date. Each binding, including after checking or unchecking a task or reusing a view, must replace the previous text and visibility so the display matches the current task.

**Pre-conditions**
- `textView` is a valid Android `TextView`, and calls execute on the UI thread. This specification covers non-null tasks.
- Each integration test captures today=LocalDate.now() once and derives due dates from that value. The device date must remain the same until assertions finish; do not change the device clock. If the date changes at midnight, discard that run and retry with a newly captured date.
- In the Tips, `Task(dueDate=..., isCompleted=...)` means `Task(title="Task", description="", id="task-1", dueDate=..., isCompleted=...)`. Relative date expressions are exact inputs: e.g. if today=2026-10-14, today.plusDays(6)=2026-10-20 and today.minusDays(6)=2026-10-08. The expected labels below are literal values independent of the run date.
- A completion change is supplied by rebinding the updated task to this function; automatic observation or scheduling of a binding is outside a direct call's responsibility.

**Post-conditions**
- Success cases
  - An active task with a future due date displays its whole-calendar-day countdown and is visible. `[Integration]`
    - Tip: Given today=LocalDate.now(), a TextView with text=`""` and visibility=View.GONE, and Task(dueDate=today.plusDays(6), isCompleted=false)/ When setDueDateLabel is called/ Then textView.text.toString() is exactly `D-6` and visibility is View.VISIBLE.
  - An active task due today displays `D-Day` and is visible. `[Integration]`
    - Tip: Given today=LocalDate.now(), a TextView with text=`""` and visibility=View.GONE, and Task(dueDate=today, isCompleted=false)/ When setDueDateLabel is called/ Then textView.text.toString() is exactly `D-Day` and visibility is View.VISIBLE.
  - An active task with a past due date displays its whole-calendar-day overdue count and is visible. `[Integration]`
    - Tip: Given today=LocalDate.now(), a TextView with text=`""` and visibility=View.GONE, and Task(dueDate=today.minusDays(6), isCompleted=false)/ When setDueDateLabel is called/ Then textView.text.toString() is exactly `D+6` and visibility is View.VISIBLE.
  - An active task without a due date clears the text and hides the label with View.GONE. `[Integration]`
    - Tip: Given today=LocalDate.now(), a TextView with text=`D-6` and visibility=View.VISIBLE, and Task(dueDate=null, isCompleted=false)/ When setDueDateLabel is called/ Then textView.text.toString() is exactly `""` and visibility is View.GONE.
  - A completed task displays `CLEAR` and is visible regardless of due date. `[Integration]`
    - Tip: Given today=LocalDate.now(), a TextView with text=`""` and visibility=View.GONE, and separate tasks with isCompleted=true and dueDate=null, today.minusDays(6), today, and today.plusDays(6)/ When setDueDateLabel is called for each task/ Then after every call textView.text.toString() is exactly `CLEAR` and visibility is View.VISIBLE.
- Error cases
  - None.
- Edge cases
  - Dates immediately before, on, and after today produce the three neighboring labels. `[Integration]`
    - Tip: Given today=LocalDate.now(), a TextView with text=`""` and visibility=View.GONE, and active tasks with dueDate=today.minusDays(1), today, and today.plusDays(1)/ When setDueDateLabel is called for each task in order/ Then texts are exactly `D+1`, `D-Day`, and `D-1`, respectively, and visibility is View.VISIBLE after every call.
  - Long countdowns spanning at least one month retain the total calendar-day count. `[Integration]`
    - Tip: Given today=LocalDate.now(), a TextView with text=`""` and visibility=View.GONE, and active tasks with dueDate=today.plusDays(30), today.plusDays(31), and today.plusDays(32)/ When setDueDateLabel is called for each task in order/ Then texts are exactly `D-30`, `D-31`, and `D-32`, respectively, and visibility is View.VISIBLE after every call.
  - Long countdowns of 364, 365, and 366 calendar days retain the total day count. `[Integration]`
    - Tip: Given today=LocalDate.now(), a TextView with text=`""` and visibility=View.GONE, and active tasks with dueDate=today.plusDays(364), today.plusDays(365), and today.plusDays(366)/ When setDueDateLabel is called for each task in order/ Then texts are exactly `D-364`, `D-365`, and `D-366`, respectively, and visibility is View.VISIBLE after every call.
  - Checking and unchecking a dated task updates the same view each time for past, present, and future dates. `[Integration]`
    - Tip: Given today=LocalDate.now(), a TextView with text=`""` and visibility=View.GONE, and dueDate separately equal to today.minusDays(1), today, and today.plusDays(1)/ When setDueDateLabel is called for each date with Task(isCompleted=false), then Task(isCompleted=true), then Task(isCompleted=false), retaining that date/ Then text sequences are exactly (`D+1`, `CLEAR`, `D+1`), (`D-Day`, `CLEAR`, `D-Day`), and (`D-1`, `CLEAR`, `D-1`), respectively, and visibility is View.VISIBLE after every call.
  - Checking and unchecking an undated task toggles between no label and `CLEAR`, removing stale completion text. `[Integration]`
    - Tip: Given today=LocalDate.now() and a TextView with text=`D-6` and visibility=View.VISIBLE/ When setDueDateLabel binds Task(dueDate=null, isCompleted=false), then Task(dueDate=null, isCompleted=true), then Task(dueDate=null, isCompleted=false)/ Then (textView.text.toString(), visibility) is exactly (`""`, View.GONE), (`CLEAR`, View.VISIBLE), and (`""`, View.GONE), respectively.
  - Reusing a view across present, absent, and present due dates replaces both its text and visibility. `[Integration]`
    - Tip: Given today=LocalDate.now() and a TextView with text=`""` and visibility=View.GONE/ When setDueDateLabel binds Task(dueDate=today.plusDays(1), isCompleted=false), then Task(dueDate=null, isCompleted=false), then Task(dueDate=today.minusDays(1), isCompleted=false)/ Then (textView.text.toString(), visibility) is exactly (`D-1`, View.VISIBLE), (`""`, View.GONE), and (`D+1`, View.VISIBLE), respectively.

**Undefined behavior** (optional)
- Passing a null TextView through another language, calling outside the Android UI thread, or changing the local calendar date during a call is outside these pre-conditions.
- This direct-call contract does not specify automatic midnight refresh or checkbox event wiring. It specifies the result whenever the adapter is invoked with the current task. Fixed month-end, year-end, and leap-day boundary examples remain in DueDateUtils.spec.md, whose function accepts today explicitly.
- The feature requirements do not define behavior for task=null; no expected result or test is prescribed for that input.
