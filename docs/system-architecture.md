## System Architecture

The app uses a hybrid client-cloud architecture separating the Club Organizer Client and Student Client from the Central Cloud Backend.

<img width="2200" height="1120" alt="system-architechture- diagram" src="https://github.com/user-attachments/assets/957f4370-6e38-4625-906c-f9567b2bd048" />

**1. Authentication and Unified Client**
- **Firebase Auth**: Manages sign-in and issues tokens with role claims
- **Unified App**: A single Android app adapts its UI based on the user's role, while backend security rules apply strict permission boundaries.

**2. Offline vs. Online Execution Paths**
- **Offline Path**: When there’s no connection environments, student attendance scans are cryptographically signed and stored locally via Room and WorkManager on the student's device, ensuring no data loss.
- **Online Path**: Once connection returns, WorkManager automatically pushes pending records to the cloud.

**3. Direction of Trust & Backend Validation**
- **Untrusted Client Boundary**: The client can request actions, but the backend holds all power. Cloud Functions independently re-verify cryptographic signatures, rotating session tokens (accepting a 10–20 second window for active token transitions), event windows, and booking eligibility before updating the database or awarding any points.

**4. Backend-Managed Approvals & Security**
- **Admin Approvals**: Event state transitions (Draft -> Pending_Approval -> Approved) are managed via the backend database console (no separate admin UI).
- **Firestore Rules**: The server stops users from upgrading their accounts, approving their own events, or changing the PointLedger.

## Data Model / ER Diagram Entities

<img width="2780" height="1930" alt="er-diagram" src="https://github.com/user-attachments/assets/0080f4d1-220b-417a-adaf-0c586b0f910e" />

The relational data model has seven core entities:
- **Users**: Stores user profiles, Firebase Auth UIDs, role flags, and registered device public keys for attendance verification and device restriction.
- **Clubs**: Links club organizers to their respective campus organizations and metadata.
- **Events**: Stores event details, venue, maximum capacity, and the ApprovalStatus workflow (Draft, Pending_Approval, Approved, Rejected).
- **AttendanceSessions**: Sets up active session windows, cryptographic keys, and changing session parameters.
- **Bookings**: Tracks student bookings while checking capacity limits, student-to-event matches, and booking versions.
- **Attendance**: Tracks student attendance through QR scans, recording cryptographic signatures, timestamps, and sync status. 
- **PointLedger (Append-Only)**: Keeps a permanent record linking points directly to verified attendance or approved changes.

### Database & Backend Integrity Rules 

**1. Unique Student Bookings**: Students can only book an event once, which is blocked by deterministic booking IDs and backend transactions. 

**2. Event Capacity Enforcement**: Event capacity is checked actively to stop over-booking. Once an event is APPROVED, it’s details (time, venue, points) cannot be changed, without submitting it again.

**3. Attendance & Point Uniqueness**: Stops repeat reward payouts by checking that the PointLedger is not already mapped to a given AttendanceID. 

**4. Booking Ownership Verification**: The student scanning the QR code must match the student ID on the event booking record. 

**5. Two-Tap / Attendance Duration Rule**: Backend validation confirms both an IN (CheckInAt) and OUT (CheckOutAt) record tracking if it lasted long enough before awarding points. 

**6. Role-Based Club Management**: only approved club organizers linked through the Clubs table can create, submit, or manage events for their club. 

**7. Auditable Point Traceability**: Points use an append-only ledger, meaning that every transaction must link back to a verified AttendanceID or an approved reason, instead of just changing a balance number. 

**8. Admin Approvals**: Admins approve events directly in the backend database console, so a separate admin UI or screen is not needed. 

**9. Firestore Rules**: The server blocks users from upgrading their accounts, approving their own events, or tampering with the permanent PointLedger.

## Offline Sync Strategy

<img width="1998" height="8192" alt="Attendance-flow-diagram" src="https://github.com/user-attachments/assets/141e2188-fbd4-4bec-b59e-f05510334c3a" />

To fulfil requirement R4, the app uses a decentralized local-first queuing pattern using Room and Android WorkManager.

**1. Pre-Event Caching & Versioning**: Before going offline, the app caches active events, bookings, and signed, temporary session tokens that have version timestamps and cryptographic nonces.

**2. Offline Capture & Local Validation**: Offline QR scans use Biometric prompt and validate against cached local data, while recording wall-clock and monotonic timestamps to detect time inconsistencies. 

**3. Local Persistence & Retries**: Signed attendance records are saved locally through Room with a pending status. WorkManager automatically handles batch uploads, spacing out retries if the first attempts fail after the connection comes back. 

**4. Background Sync & Backend Verification**: When connection is back, Firebase Cloud Functions validates signatures, checks rotation windows, handles deduplication, and makes sure they still qualify before awarding points. 

**5. Sync Transparency & State Separation**: The UI clearly differentiates local states from server verification using visuals: an amber colour for local device capture and a green checkmark for confirmed server sync.

| **Timeline / State**                                  | **System Behaviour & Data Flow**                                                                                                                                                                        |
| ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **10:00 AM (Check-In Scan)**                          | Club projects a rotating IN QR code. Students verify with a biometric prompt, scan the code, and the app locally verifies credentials against the Room cache to log the check-in time.                  |
| **10:00 AM (Local Persistence)**                      | The app cryptographically signs the record and writes it to the local Room database as "pending" so it can work offline.                                                                                |
| **11:45 AM (Check-Out Scan)**                         | Club displays the OUT QR code for the final 15 minutes. Students re-verify with biometrics and scan to record their check-out time.                                                                     |
| **12:00 PM (Connection Restored)**                    | WorkManager detects the connection coming back and starts a background upload of pending records to Firebase.                                                                                           |
| **12:01 PM (Backend Verification and Ledger Update)** | Firebase Cloud Functions validates the batch by checking the cryptographic signatures, session windows, duration, uniqueness, evaluating time evidence, and officially committing points to the ledger. |

## Attendance Security Analysis

To meet requirement **R3**, potential cheating methods and defences have been analysed:

| **Cheating Method**                    | **Defence**                                                                                           | **How it's still weak**                                                                                             |
| -------------------------------------- | ----------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| **Proxy Check-In / Account Sharing**   | Mandatory biometric prompt (fingerprint/face unlock) on the student's device before scanning.         | A student could still lend their unlocked phone to another if the app is left unlocked.                             |
| **QR Code Relay / Screenshot Sharing** | Rotating QR codes changing every 10–20 seconds via project screen mirroring.                          | A livestream of the projector could still allow remote scanning during the active window.                           |
| **Leaving Early / Partial Attendance** | Two-tap verification needing both an IN scan at the start and an OUT scan during the last 15 minutes. | A student could still scan in, leave the venue, and come back in time for the check-out window.                     |
| **Post-Event Check-In**                | The server checks temporary session tokens when syncing                                               | Offline devices rely on their internal clocks, which users could change to cheat the system.                        |
| **Repeat Submissions & Replays**       | Cryptographic signatures, unique transaction UUIDs, and idempotent backend processing.                | Relies on correct backend deduplication and signature verification configuration.                                   |
| **Local Storage Tampering**            | Android app sandboxing, secure SQLite/Room storage, and immediate cryptographic signing.              | Very modified or rooted devices could attempt to manipulate local pending records before sync.                      |
| **Clashing Event Bookings**            | Backend schedule checks that reject overlapping timestamps during cloud synchronization.              | Conflicts can happen if an approved event schedule is changed by an organizer while a student is offline.           |
| **Multi-Device Account Sharing**       | Restrict each student account to one registered device using device-specific public keys.             | Students might transfer accounts to another device. Device registration cannot ensure the actual owner is using it. |
