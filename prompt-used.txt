I want to generate simple work-time tracking application for native android, using android api level 28.

Application should have normal android settings, where two items could be configured:
1. "starting date" in format YYYY-mm-dd  (this will be a placeholder for a future - will not be used initially)
2. "starting overtime" amount, in format +/- dd:HH:mm  (days:hours:minutes)

I would like it to be generated in kotlin. I would want that main screen had vertically split layout,

upper having one button and "status line" (defined at the bottom of this message). Button should have two states - a) pressed=counting time, b) depressed-not counting time. Lower showing "scrollview of entries" (in format `YYYY-mm-dd HH-mm` | `YYYY-mm-dd HH-mm` | `HH:mm`) where single row would have three items separated with pipe character: start timestamp | end timestamp | total time for row, difference between end timestamp and start timestamp in format hours:minutes.
When button is pressed, an entry is added to application's database, storing the timestamp;


"status line" should be single text view, showing result of calculation:
For each entry in database we should be able to calculate total time at work spend during single day. When the time logged exceeds 08 hours 00 minutes - time would increase value of "starting overtime". When the time logged next day would be less than 8h it would reduce the starting overtime. Two examples:
1.
a) user starts application 1st time and it is 2024-03-03 08:00, starting overtime is set to 00:00:00.
b) user starts counting time pressing the button. Starting timestamp is logged and visible in "scrollview of entries"
c) after 6h user de-presses the button - it is 2024-03-03 14:00, second timestamp is logged, and visible in "scrollview of entries"
d) the row in "scrollview of entries" for this day looks like this:  `2024-03-03 08:00 | 2024-03-03 14:00 | -02:00`
e) the "starting overtime"  value is updated, to existing value -00:02:00 is added (so here, given it is less than 8h it is substracted)
f) "starting overtime" equals now -02:00
2.
a) next day  2024-03-04, user starts application again
b) user starts counting time pressing the button at 2024-03-04 08:00. New row is visible in scroll view.
c) after 13h user de-presses the button - it is  2024-03-04 21:00. timestamp is logged, and visible in "scrollview of entries"
d) two rows are visible in "scrollview of entries (for days 2024-03-03 and 2024-03-04)
e) "starting overtime" value is again updated, +00:05:00 is added to existing value, yielding +00:03:00


I want the application to be as simple as possible, it does not need to contain any progruard or linting stuff. You can also make assumptions when data is not provided - for example - by making reasonable assumptions like: if button dimensions are needed use default or assume it is always 50% of container.
