# Limitations

This document outlines which functionality is not available in the current version of the app.

## Overlapping sessions

The main schedule screen groups sessions by room. Within each room column, sessions are vertically
listed in the order in which they start. The session card occupies the vertical space according to
the duration of the session.

If the schedule data provided by the backend contains overlapping sessions, the app must decide
how to display them. The current implementation does not support showing overlapping sessions.

If a later session starts before an earlier session ends, than the **earlier session card** is
**cut off**, and the later session card is displayed right below it positioned according to its
start time.

```
Time  | Room with two overlapping sessions
------|-----------------------------------------------
      |
9:00  |  Session S1
      |  9:00 - 11:00
      |
10:00 |
      |
10:30 |-----------------------------------------------
      |  Session S2
      |  10:30 - 12:30    <-- Starts before S1 ends
11:00 |
      |
11:30 |
      |
12:00 |
      |
12:30 |
```

## Non-unique session IDs

Schedule data can originate from one backend system. In another event setup, the schedule data from
multiple backends is **merged** into a single file.

In both cases, it is possible for two sessions to have the **same ID**. If this happens, then the
app will not be able to display the sessions correctly. It will **only show one session** and ignore
the other. Which session is shown is **unpredictable**.

Another scenario to consider is when the backend system supports setting up a session being
**repeated multiple times**. This saves the organizer from having to create multiple sessions for
the same content. These sessions are given the **same ID**. However, the app will not be able to
display these sessions correctly for the same reasons as described above.

```
SCHEDULE DATABASE
   +-------------------------------------+
   | ID  | Title     | Room name | Time  |
   +-------------------------------------+
   | 301 | Session X | Room 1    | 10:00 |
   +-------------------------------------+
   | 301 | Session Y | Room 2    | 11:00 |  <-- Duplicate ID
   +-------------------------------------+
   | 302 | Session Z | Room 1    | 12:00 |
   +-------------------------------------+
      │
      ▼
APP DISPLAY
   +---------------------------------------------------+
   | Time      |        Room 1         |     Room 2    |
   +---------------------------------------------------+
   | 10:00     |  Session X (ID: 301)  |               |
   +---------------------------------------------------+
   | 11:00     |                       |               |
   +---------------------------------------------------+
   | 12:00     |  Session Z (ID: 302)  |               |
   +---------------------------------------------------+
```
