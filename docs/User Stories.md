**M \= Must-have      N \= Nice-to-have**

## Student

### Account
| ID | User Story | Acceptance Criteria | Priority |
| :---- | :---- | :---- | :---- |
| S1 | I want to log in and enrol my device (R6) | Given I select Student and enter a valid email and password, when I tap Login, I will reach the Discover page. If the details are empty or incorrect, I see an error message and stay on the login page.| M |
| S2 | I want to filter approved events by name, club, date and points so that I find relevant events quickly. (R2)  | Given approved and pending events exist, when I open the events page, only approved events appear.  Given no event matches my search, I will see an empty state.  | 
| S3 | I want to book a place to guarantee my attendance. (R2)   | Given a place is available, when I tap “Book”, then I see the event in my Profile and the available seats decrease by one. If two students book the last seat at once, only one succeeds and the other is notified it is full. | M  |
| S4 | I want to cancel my booking so that my seat can go to someone else if I can no longer attend. (R2) | Given I have booked an event, when I tap “Cancel” on the event, then the event is removed from my Profile and the available seats increase by one. If students are on the waitlist, the first student is notified that a seat is available. | M |
| S5  | I want to know if an event is full so that I don’t waste time trying to book it. (R2)  | Given an event is full, when I view it, then the “Book” button is replaced by "Full, Join Waitlist". If a seat becomes available, students on the waitlist are notified in the order they joined. | M  |
| S6 | I want a warning when events overlap so that I don’t double-book myself. (R2) | Given I have booked an event from 2 to 4 pm, when I try to book one from 3 to 5 pm, then I am notified of the clash and the booking is blocked. If the new event starts exactly when my booked event ends, at 4 pm, the booking is allowed. | M |
| S7 | I want to scan the QR to check in so that my attendance counts without queueing at a sign-in sheet.  (R3)  | Check-in is open for as long as the club chooses to show the check-in QR. Given check-in is open, when I scan the QR and pass the biometric check, then my check-in is recorded. If the event is registration-only and I have not registered, my biometrics do not match the ones on my phone, or check-in is closed, I am notified that nothing is recorded. | M |
| S8 | I want to check out at the end so that my full stay is proven and I earn the points.  (R3)  | Given I checked in and check-out is open, when I scan the check-out QR and pass the biometric check, then my check-out is recorded. If I did not check in or check-out is closed, I am notified that nothing is recorded. | M |
| S9 | I have no signal, I want to check in anyway so that I don’t lose my points.  (R4)  | Given I am offline, when I scan the QR and pass the biometric check, then my record is saved on my phone as Pending and will sync when I am back online. Pending records can be synced up to 3 months after the event; after that they show as Failed with a reason. | M |
| S10 | I want to see what has synced so that I know my attendance counts.  (R4)  | Given I have Pending records, when I am back online, the records sync automatically and show as Success or Failed. If a record has failed, I can open it to see the reason. | M |
| S11 | I want to see my points and history so that I can track progress towards my s-CPD target. (R5)  | Given my check-in and check-out are verified and I stayed for the minimum time set by the club, which is 20 minutes by default, when I open my points, the event’s points are added to my total. If my check-out is missing, the event shows “Incomplete”. | M |
| S12 | I want to see why points were removed so that I can appeal if it was a mistake.  (R5)  | Given a record is later found invalid, when I open my points, then I see the points removed and the reason.  | M |
| S13 | I want reminders for booked events and check-out so that I don’t miss points.  | Given a booked event, when it is 1 hour away or the check-out window opens, then I am notified.  | N |

**Cheating and exceptional cases**

| ID | Attempt | Result |
| :---- | :---- | :---- |
| X1 | A friend checks in for me with my login on their phone | Rejected: only my enrolled phone holds the signing key for my account, so a check-in from any other phone cannot be signed. |
| X2 | I scan from home using a forwarded photo of the QR | Rejected: each QR is valid for 10 seconds and must still be valid after the biometric check, so a forwarded photo expires before it can be used. |
| X3 | I scan after the event and sync later | Rejected: each QR carries the time slot it was shown in, set by the server, so a record from outside the check-in or check-out window fails when it syncs. |
| X4 | I edit the database to modify event time | Rejected: each QR is sealed with a secret key that only the server knows. If the time or event in my record is changed, the seal no longer matches and the record fails when it syncs.|
| X5 | I send the same attendance record more than once to get extra points | Rejected: each record has a unique ID, and the server accepts only one check-in and one check-out per student per event, so copies are ignored. |
| X6 | I forgot to check out | The attendance is marked Incomplete and no points are given. The club can complete my check-out (C7). |
| X7 | My phone is off or offline when the sync window closes | The record shows as Failed after 3 months with the reason “Sync deadline passed” and no points are given. |

**Club committee** 

| ID | Story | Acceptance Criteria | Priority |
| :---- | :---- | :---- | :---- |
| C1  | I want to create an event and request s-CPD points for it  (R1)  | Given I have filled in all required fields, when I tap “Submit for approval”, then the event is saved with the status “Pending approval”. Given a required field is empty or the end time is before the start time, when I tap “Submit”, then the invalid field is highlighted with a message and nothing is sent.  | M |
| C2 | I want to see whether my event is pending, approved or rejected so that I know when I can promote it or fix my request. (R1)  | Given my event has been submitted, When I open “My events”, then I see its status as Pending, Approved or Rejected. Given the admin rejected my event, When I open it, then I see “Rejected” with the admin’s reason and an option to submit a new event.  | M |
| C3 | I want to cancel an approved event if plans change so that booked students are told and don’t turn up for nothing.  (R1)  | Given I confirm the cancellation, when it saves, then the event shows “Cancelled” and every booked student is notified.  | M |
| C4 | I want a full-screen rotating QR that I can mirror to a laptop or projector so that the whole room can check in and check out at once.  (R3)  | Given the session is open, when Projector mode is on, a full-screen, high-contrast rotating QR refreshes every 10 seconds. It can be mirrored to a laptop or projector.   | M |
| C5 | I want the QR to keep rotating without a signal so that a bad hall connection doesn’t stop my event.  (R4)  | Given I tapped “Prepare event” while online, When the connection drops, then the QR keeps refreshing.  Given I never prepared the event, when I open Projector mode offline, then I see “Connect once to prepare this event”.  | M |
| C6 | I want a live check-in count so that I can see turnout as it happens.  | Given the session is open, When students check in, then the counter updates within seconds.  | N |
| C7 | I want to record students who left early with permission or forgot to check out so that honest students aren’t denied points for a slip.   (R3, R5) | Given an event has ended and a student checked in but has no check-out, when I complete their check-out, then their attendance is completed. | M |
| C8 | Be unable to approve my own events (R6)  | Club accounts can’t change an event’s approval status.  | M |
| C9 | I want to log in as a committee member so that I can manage my club’s events. (R6) | Given valid login details, when I tap Login, then the database checks my club ID and I reach my club’s events. If I have no club ID or the club is inactive, I see an error. | M |
| C10 | I want to edit my event while it is pending approval so that I can fix mistakes before it is approved. (R1) | Pending events can be edited and stay pending after saving. Approved events cannot be edited, only cancelled. Rescheduling an approved event must go through the s-CPD office. | M |
| C11 | I want to choose when check-in and check-out are open so that students only record attendance while the QR is shown. (R3) | Tapping the middle button on the Check In or Check Out tab shows the rotating QR. Scans while closed are not recorded. | M |
| C12 | I want to see my club’s members and committee so that I know who is in my club. (R6) | Committee members can view and search the members and committee lists. Only the president can add or remove people and edit club details; the server refuses anyone else. | N |

**Admin (s-CPD office)**

| ID | Story | Acceptance Criteria | Priority |
| :---- | :---- | :---- | :---- |
| A1 | I want to approve or reject clubs’ event requests with a reason so that only worthwhile events award points to students.  (R1)  | Given an event has the status “Pending approval”, when I set its status to Approved in the backend database, then the event appears on the student Events page and the club sees “Approved”. Given an event has the status “Pending approval”, when I set it to Rejected and enter a reason, then it never appears on the Events page and the club sees “Rejected” with my reason.  | M |
| A2 | I want to invalidate a record so that unearned points are removed.  (R5)  | Given I mark a record invalid, when the change saves, then a reversing ledger entry is added and the total updates.  | M |
| A3 | I want each role limited to its own actions so that nobody can award themselves points.  (R6)  | Given a club account, when it tries to change an event’s approval status, then the server refuses.  Given I sign up through the app, when my account is created, then I have only the student role.  | M |
| A4 | I want suspicious attendance flagged for review so that I can remove unearned points before they count towards a student’s total.  | Given an unusually large share of an event’s check-ins come from one network, when the sync finishes, then the event is flagged for review rather than rejected automatically. Given a student has repeatedly rejected records, when I open the flag list in the backend, then the student appears with the rejection reasons for each record.  | N |

**Own features**

| ID | Story | Acceptance Criteria | Priority |
| :---- | :---- | :---- | :---- |
| O1 | As a student, I want to see the events I signed up for on a calendar so that I can plan my week and don’t miss events. | Given I have signed up for events, when I open the Calendar, then each event is shown as a coloured line under its date. When an event on my calendar is about to start, I am notified. | N |
| O2 | As a club committee member, I want to see my club’s events on a calendar so that I can plan new events around existing ones. | Given my club has events, when I open the Calendar, then each event is shown as a coloured line under its date. When an event on my calendar is about to start, I am notified. | N |
| O3 | As a club committee member, I want to see my club’s statistics so that I can tell which events work well and plan better ones. | Given my club has held events, when I open Statistics, then I see my club’s overall statistics across all its events and the number of events per month and per year. When I open an event’s statistics, I see its views, sign-ups and participants. | N |
