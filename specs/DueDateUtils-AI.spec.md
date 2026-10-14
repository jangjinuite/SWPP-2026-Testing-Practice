# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `internal fun dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Return the task's due-date label using the signed number of whole calendar days from today to its due date. Completed tasks always display `CLEAR`; active tasks without a due date display no label.

**Pre-conditions**
- `today` is a valid non-null `LocalDate`; `dueDate` is a valid `LocalDate` or `null`, and either Boolean completion state is accepted.
- Dates denote calendar dates, without times or time zones; the caller supplies the date to use as today.

**Post-conditions**
- Success cases
  - A future due date on an active task returns `D-N`, where N is the total calendar-day distance. `[Unit]`
    - Tip: Given dueDate=2026-10-20, today=2026-10-14, isCompleted=false/ When dueDateLabel is called/ Then the result is exactly `D-6`.
  - An active task due today returns `D-Day`. `[Unit]`
    - Tip: Given dueDate=2026-10-14, today=2026-10-14, isCompleted=false/ When dueDateLabel is called/ Then the result is exactly `D-Day`.
  - A past due date on an active task returns `D+N`, where N is the total calendar-day distance. `[Unit]`
    - Tip: Given dueDate=2026-10-08, today=2026-10-14, isCompleted=false/ When dueDateLabel is called/ Then the result is exactly `D+6`.
  - An active task with no due date returns the empty string. `[Unit]`
    - Tip: Given dueDate=null, today=2026-10-14, isCompleted=false/ When dueDateLabel is called/ Then the result is exactly `""`.
  - Completion overrides every due-date category, including no date. `[Unit]`
    - Tip: Given today=2026-10-14, isCompleted=true, and dueDate separately equal to null, 2026-10-08, 2026-10-14, and 2026-10-20/ When dueDateLabel is called for each input/ Then each result is exactly `CLEAR`.
- Error cases
  - None. All inputs satisfying the pre-conditions are valid, including absent and past due dates. `[Unit]`
    - Tip: Given today=2026-10-14, isCompleted=false, and dueDate separately equal to null and 2026-10-13/ When dueDateLabel is called for each input/ Then it returns exactly `""` and `D+1`, respectively, without throwing.
- Edge cases
  - The past/today/future boundary is correct on the boundary and one day on either side. `[Unit]`
    - Tip: Given today=2026-10-14, isCompleted=false, and dueDate separately equal to 2026-10-13, 2026-10-14, and 2026-10-15/ When dueDateLabel is called for each input/ Then results are exactly `D+1`, `D-Day`, and `D-1`, respectively.
  - Calendar-day distances remain correct immediately before, on, and after a month transition. `[Unit]`
    - Tip: Given today=2026-01-30, isCompleted=false, and dueDate separately equal to 2026-01-31, 2026-02-01, and 2026-02-02/ When dueDateLabel is called for each input/ Then results are exactly `D-1`, `D-2`, and `D-3`, respectively.
  - Whole months contribute all their days, in either direction, including dates just before and after a full month. `[Unit]`
    - Tip: Given today=2026-01-15, isCompleted=false, and dueDate separately equal to 2026-02-14, 2026-02-15, and 2026-02-16/ When dueDateLabel is called for each input and again with today and dueDate swapped/ Then forward results are exactly `D-30`, `D-31`, and `D-32`, and swapped results are exactly `D+30`, `D+31`, and `D+32`, respectively.
  - Calendar-day distances remain correct immediately before, on, and after a year transition. `[Unit]`
    - Tip: Given today=2026-12-30, isCompleted=false, and dueDate separately equal to 2026-12-31, 2027-01-01, and 2027-01-02/ When dueDateLabel is called for each input and again with today and dueDate swapped/ Then forward results are exactly `D-1`, `D-2`, and `D-3`, and swapped results are exactly `D+1`, `D+2`, and `D+3`, respectively.
  - A full non-leap year contributes 365 days, including the adjacent dates. `[Unit]`
    - Tip: Given today=2025-01-01, isCompleted=false, and dueDate separately equal to 2025-12-31, 2026-01-01, and 2026-01-02/ When dueDateLabel is called for each input/ Then results are exactly `D-364`, `D-365`, and `D-366`, respectively.
  - Leap day counts as one calendar day, with checks immediately before, on, and after it in both directions. `[Unit]`
    - Tip: Given today=2028-02-27, isCompleted=false, and dueDate separately equal to 2028-02-28, 2028-02-29, and 2028-03-01/ When dueDateLabel is called for each input and again with today and dueDate swapped/ Then forward results are exactly `D-1`, `D-2`, and `D-3`, and swapped results are exactly `D+1`, `D+2`, and `D+3`, respectively.
  - A non-leap February has no extra day at its month transition. `[Unit]`
    - Tip: Given today=2026-02-27, isCompleted=false, and dueDate separately equal to 2026-02-28, 2026-03-01, and 2026-03-02/ When dueDateLabel is called for each input/ Then results are exactly `D-1`, `D-2`, and `D-3`, respectively.
  - Switching completion on and off restores the label for the same date or absence of a date. `[Unit]`
    - Tip: Given today=2026-10-14 and dueDate separately equal to null, 2026-10-13, 2026-10-14, and 2026-10-15/ When dueDateLabel is called with isCompleted=false, then true, then false for each due date/ Then result sequences are exactly (`""`, `CLEAR`, `""`), (`D+1`, `CLEAR`, `D+1`), (`D-Day`, `CLEAR`, `D-Day`), and (`D-1`, `CLEAR`, `D-1`), respectively.

**Undefined behavior** (optional)
- Invalid date strings are outside this function's contract because it accepts `LocalDate`, not text; passing null for the non-null `today` parameter from another language is outside the Kotlin contract.
