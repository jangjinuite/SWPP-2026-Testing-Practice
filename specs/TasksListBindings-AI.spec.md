# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Bind the current task's due-date label to its TextView using today's local calendar date. Each binding, including after checking or unchecking a task or reusing a view, must replace the previous text and visibility so the display matches the current task.

**Pre-conditions**
- `textView` is a valid Android `TextView`, and calls execute on the UI thread; `task` may be null.
- The local date is stable during each call. For every Tip below, the test environment's local date is fixed to 2026-10-14 unless another date is stated explicitly.
- In the Tips, `Task(dueDate=..., isCompleted=...)` means `Task(title="Task", description="", id="task-1", dueDate=..., isCompleted=...)`; ISO date values denote `LocalDate` values.
- A completion change is supplied by rebinding the updated task to this function; automatic observation or scheduling of a binding is outside a direct call's responsibility.

**Post-conditions**
- Success cases
  - An active task with a future due date displays its whole-calendar-day countdown and is visible. `[Integration]`
    - Tip: Given local today=2026-10-14, a TextView with text=`""` and visibility=View.GONE, and Task(dueDate=2026-10-20, isCompleted=false)/ When setDueDateLabel is called/ Then textView.text.toString() is exactly `D-6` and visibility is View.VISIBLE.
  - An active task due today displays `D-Day` and is visible. `[Integration]`
    - Tip: Given local today=2026-10-14, a TextView with text=`""` and visibility=View.GONE, and Task(dueDate=2026-10-14, isCompleted=false)/ When setDueDateLabel is called/ Then textView.text.toString() is exactly `D-Day` and visibility is View.VISIBLE.
  - An active task with a past due date displays its whole-calendar-day overdue count and is visible. `[Integration]`
    - Tip: Given local today=2026-10-14, a TextView with text=`""` and visibility=View.GONE, and Task(dueDate=2026-10-08, isCompleted=false)/ When setDueDateLabel is called/ Then textView.text.toString() is exactly `D+6` and visibility is View.VISIBLE.
  - An active task without a due date clears the text and hides the label with View.GONE. `[Integration]`
    - Tip: Given local today=2026-10-14, a TextView with text=`D-6` and visibility=View.VISIBLE, and Task(dueDate=null, isCompleted=false)/ When setDueDateLabel is called/ Then textView.text.toString() is exactly `""` and visibility is View.GONE.
  - A completed task displays `CLEAR` and is visible regardless of due date. `[Integration]`
    - Tip: Given local today=2026-10-14, a TextView with text=`""` and visibility=View.GONE, and separate tasks with isCompleted=true and dueDate=null, 2026-10-08, 2026-10-14, and 2026-10-20/ When setDueDateLabel is called for each task/ Then after every call textView.text.toString() is exactly `CLEAR` and visibility is View.VISIBLE.
  - A null task clears the text and hides the label with View.GONE. `[Integration]`
    - Tip: Given local today=2026-10-14, a TextView with text=`CLEAR` and visibility=View.VISIBLE, and task=null/ When setDueDateLabel is called/ Then textView.text.toString() is exactly `""` and visibility is View.GONE.
- Error cases
  - None. Every task value permitted by the signature, including null and a task without a due date, is accepted. `[Integration]`
    - Tip: Given local today=2026-10-14 and a TextView with text=`D-Day` and visibility=View.VISIBLE/ When setDueDateLabel is called first with task=null and then with Task(dueDate=null, isCompleted=false)/ Then neither call throws, and after each call textView.text.toString() is exactly `""` and visibility is View.GONE.
- Edge cases
  - Dates immediately before, on, and after today produce the three neighboring labels. `[Integration]`
    - Tip: Given local today=2026-10-14, a TextView with text=`""` and visibility=View.GONE, and active tasks with dueDate=2026-10-13, 2026-10-14, and 2026-10-15/ When setDueDateLabel is called for each task in order/ Then texts are exactly `D+1`, `D-Day`, and `D-1`, respectively, and visibility is View.VISIBLE after every call.
  - Month transitions retain the total calendar-day count, including either side of a complete month. `[Integration]`
    - Tip: Given local today=2026-01-15, a TextView with text=`""` and visibility=View.GONE, and active tasks with dueDate=2026-02-14, 2026-02-15, and 2026-02-16/ When setDueDateLabel is called for each task in order/ Then texts are exactly `D-30`, `D-31`, and `D-32`, respectively, and visibility is View.VISIBLE after every call.
  - Dates immediately before, on, and after a year transition use calendar-day counts. `[Integration]`
    - Tip: Given local today=2026-12-30, a TextView with text=`""` and visibility=View.GONE, and active tasks with dueDate=2026-12-31, 2027-01-01, and 2027-01-02/ When setDueDateLabel is called for each task in order/ Then texts are exactly `D-1`, `D-2`, and `D-3`, respectively, and visibility is View.VISIBLE after every call.
  - Dates immediately before, on, and after leap day count that day exactly once. `[Integration]`
    - Tip: Given local today=2028-02-27, a TextView with text=`""` and visibility=View.GONE, and active tasks with dueDate=2028-02-28, 2028-02-29, and 2028-03-01/ When setDueDateLabel is called for each task in order/ Then texts are exactly `D-1`, `D-2`, and `D-3`, respectively, and visibility is View.VISIBLE after every call.
  - Checking and unchecking a dated task updates the same view each time for past, present, and future dates. `[Integration]`
    - Tip: Given local today=2026-10-14, a TextView with text=`""` and visibility=View.GONE, and dueDate separately equal to 2026-10-13, 2026-10-14, and 2026-10-15/ When setDueDateLabel is called for each date with Task(isCompleted=false), then Task(isCompleted=true), then Task(isCompleted=false), retaining that date/ Then text sequences are exactly (`D+1`, `CLEAR`, `D+1`), (`D-Day`, `CLEAR`, `D-Day`), and (`D-1`, `CLEAR`, `D-1`), respectively, and visibility is View.VISIBLE after every call.
  - Checking and unchecking an undated task toggles between no label and `CLEAR`, removing stale completion text. `[Integration]`
    - Tip: Given local today=2026-10-14 and a TextView with text=`D-6` and visibility=View.VISIBLE/ When setDueDateLabel binds Task(dueDate=null, isCompleted=false), then Task(dueDate=null, isCompleted=true), then Task(dueDate=null, isCompleted=false)/ Then (textView.text.toString(), visibility) is exactly (`""`, View.GONE), (`CLEAR`, View.VISIBLE), and (`""`, View.GONE), respectively.
  - Reusing a view across present, absent, and present due dates replaces both its text and visibility. `[Integration]`
    - Tip: Given local today=2026-10-14 and a TextView with text=`""` and visibility=View.GONE/ When setDueDateLabel binds Task(dueDate=2026-10-15, isCompleted=false), then Task(dueDate=null, isCompleted=false), then Task(dueDate=2026-10-13, isCompleted=false)/ Then (textView.text.toString(), visibility) is exactly (`D-1`, View.VISIBLE), (`""`, View.GONE), and (`D+1`, View.VISIBLE), respectively.
  - Reusing a view across a task, null, and another task clears and restores the label. `[Integration]`
    - Tip: Given local today=2026-10-14 and a TextView with text=`""` and visibility=View.GONE/ When setDueDateLabel binds Task(dueDate=null, isCompleted=true), then task=null, then Task(dueDate=2026-10-14, isCompleted=false)/ Then (textView.text.toString(), visibility) is exactly (`CLEAR`, View.VISIBLE), (`""`, View.GONE), and (`D-Day`, View.VISIBLE), respectively.

**Undefined behavior** (optional)
- Passing a null TextView through another language, calling outside the Android UI thread, or changing the local calendar date during a call is outside these pre-conditions.
- This direct-call contract does not specify automatic midnight refresh, checkbox event wiring, or how the test environment fixes the local date. It specifies the result whenever the adapter is invoked with the current task.
