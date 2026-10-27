**M \= Must-have      N \= Nice-to-have**

**Student**

| ID | User Story | Acceptance Criteria | Priority |
| :---- | :---- | :---- | :---- |
| S1 | I want to log in and enrol my device (R6) | Given valid credentials, when I tap Login, Then I reach the marketplace.  Given a wrong password, when I tap Login, then I see “Email or password is incorrect”.  | M |
| S2 | I want to filter approved events by name, club, date and points so that I find relevant events quickly. (R2)  | Given approved and pending events exist, when I open the marketplace, then only approved events appear.  Given no event matches my search, When results load, then I see an empty state with a “Clear filters” button.  | M  |
| S3 | I want to book a place to guarantee my attendance. (R2)   | Given a place is available, When I tap “Book”, then the event appears in “My events” and the seat count drops by one.  Given two students book the last place at once, when both requests arrive, then one succeeds and the other sees “Event full”.  | M  |
| S4  | I want to know if an event is full so that I don’t waste time trying to book it. (R2)  | Given an event is full, when I view it, then the Book is replaced by “Full”.  Given an event is full, when a place opens, then waitlisted students are notified in order.  | M  |
| S5 | I want a warning when events overlap so that I don’t double-book myself. (R2) | Given I have booked an event from 2 to 4 pm, When I book one from 3 to 5 pm, then I see the clash and the booking is blocked.  | M |
| S6 | I want to scan the QR to check in so that my attendance counts without queueing at a sign-in sheet.  (R3)  | Given I booked the event and the check-in window is open, when I scan and pass the biometric prompt, then the app shows “Recorded”.  Given I did not book the event, when I scan its QR, then I see “No booking for this event” and nothing is recorded.  | M |
| S7 | I want to check out at the end so that my full stay is proven and I earn the points.  (R3)  | Given I checked in and the check-out window is open, when I scan the OUT QR, then the check-out is recorded.  Given I scan the IN QR during check-out, when the app reads it, then it is rejected.  | M |
| S8 | I have no signal, I want to check in anyway so that I don’t lose my points.  (R4)  | Given I’m offline, when I scan and pass the biometric prompt, then the app shows “Saved, will sync” and the record is Pending.   | M |
| S9 | I want to see what has synced so that I know my attendance counts.  (R4)  | Given I have Pending records, when the connection returns, then they sync automatically and show Synced or Rejected.   Given a record is rejected, when I open it, then I see the reason.  | M |
| S10 | I want to see my points and history so that I can track progress towards my s-CPD target. (R5)  | Given my check-in and check-out are verified and I met the minimum time, When I open Points, then the event’s points appear in my total.  Given my check-out is missing, When I open Points, then the event shows “Incomplete” with no points.  | M |
| S11 | I want to see why points were removed so that I can appeal if it was a mistake.  (R5)  | Given a record is later found invalid, When I open Points, then a reversal entry shows the reason.  | M |
| S12 | I want reminders for booked events and check-out so that I don’t miss points.  | Given a booked event, when it is 1 hour away or the check-out window opens, then I get a notification.  | N |

**Cheating and exceptional cases**

| ID | Attempt | Result |
| :---- | :---- | :---- |
| X1 | A friend checks in for me with my login on their phone  | Rejected: the signing key is on my enrolled phone and needs my biometric.  |
| X2 | I scan from home using a forwarded photo of the QR  | Rejected: the token expires after 15 seconds |
| X3 | I scan after the event and sync later  | Rejected: the token’s time slot is outside the window, so a late sync doesn’t help.  |
| X4 | I edit the Room database to modify event time or resend the same record to claim s-CPD points multiple times | Rejected: the signature fails, or the nonce and the unique (event, student, type) check reject it.  |
| X5 | I forgot to check out | The attendance is marked Incomplete and no points are credited. The club can override, and the override is logged.  |
| X6 | My phone is off or offline when the sync window closes  | Rejected with a clear reason. The student can appeal to the club.  |

**Club committee** 

| ID | Story | Acceptance Criteria | Priority |
| :---- | :---- | :---- | :---- |
| C1  | I want to create an event and request s-CPD points for it  (R1)  | Given I have filled in all required fields (title, date, time, venue, capacity, points requested), when I tap “Submit for approval”, then the event is saved with the status “Pending approval”. Given a required field is empty or the end time is before the start time, when I tap “Submit”, then the invalid field is highlighted with a message and nothing is sent.  | M |
| C2 | I want to see whether my event is pending, approved or rejected so that I know when I can promote it or fix my request. (R1)  | Given my event has been submitted, When I open “My events”, then I see its status as Pending, Approved or Rejected. Given the admin rejected my event, When I open it, then I see “Rejected” with the admin’s reason and an option to submit a new event.  | M |
| C3 | I want to cancel an approved event if plans change so that booked students are told and don’t turn up for nothing.  (R1)  | Given I confirm the cancellation, when it saves, then the event shows “Cancelled” and every booked student is notified.  | M |
| C4 | I want a full-screen rotating QR that I can mirror to a laptop or projector so that the whole room can check in and check out at once.  (R3)  | Given the session is open, when Projector mode is on, a full-screen, high-contrast rotating QR refreshes every 15 seconds, It can be mirrored to a laptop or projector.   | M |
| C5 | I want the QR to keep rotating without a signal so that a bad hall connection doesn’t stop my event.  (R4)  | Given I tapped “Prepare event” while online, When the connection drops, then the QR keeps refreshing.  Given I never prepared the event, when I open Projector mode offline, then I see “Connect once to prepare this event”.  | M |
| C6 | I want a live check-in count so that I can see turnout as it happens.  | Given the session is open, When students check in, then the counter updates within seconds.  | N |
| C7 | I want to record students who left early with permission or forgot to check out so that honest students aren’t denied points for a slip.   (R3, R5) | Given an ended event, When I open Analytics, then I see booked vs attended and average time stayed.  | N |
| C8 | Be unable to approve my own events (R6)  | Club accounts can’t change an event’s approval status.  | M |

**Admin (s-CPD office)**

| ID | Story | Acceptance Criteria | Priority |
| :---- | :---- | :---- | :---- |
| A1 | I want to approve or reject clubs’ event requests with a reason so that only worthwhile events award points to students.  (R1)  | Given an event has the status “Pending approval”, when I set its status to Approved in the backend database, then the event appears in the student marketplace and the club sees “Approved”. Given an event has the status “Pending approval”, when I set it to Rejected and enter a reason, then it never appears in the marketplace and the club sees “Rejected” with my reason.  | M |
| A2 | I want to invalidate a record so that unearned points are removed.  (R5)  | Given I mark a record invalid, when the change saves, then a reversing ledger entry is added and the total updates.  | M |
| A3 | I want each role limited to its own actions so that nobody can award themselves points.  (R6)  | Given a club account, when it tries to change an event’s approval status, then the server refuses.  Given I sign up through the app, when my account is created, then I have only the student role.  | M |
| A4 | I want suspicious attendance flagged for review so that I can remove unearned points before they count towards a student’s total.  | Given an unusually large share of an event’s check-ins come from one network, when the sync finishes, then the event is flagged for review rather than rejected automatically. Given a student has repeatedly rejected records, when I open the flag list in the backend, then the student appears with the rejection reasons for each record.  | N |

