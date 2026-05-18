## 1) Project Overview

### What the app solves
- Provides a local-first mentoring marketplace simulation without backend/API setup.
- Demonstrates complete Android app architecture with role-based navigation.
- Supports classroom-like actions: course publishing, searching, enrolling, and mentor-mentee communication.

### Core design reasoning
- Keep responsibilities separated by role to reduce misuse and confusion.
- Favor simple but complete flows (detail -> payment -> confirmation).
- Protect core data consistency with repository validations and transactions.
- Maintain smoother UX with a launcher/loading screen and slide transitions.

## 2) Tech Stack

- Language: Java 11
- Build system: Gradle Kotlin DSL
- UI: XML layouts + Material Components + RecyclerView
- Local persistence: SQLite (`SQLiteOpenHelper`)
- Session handling: `SharedPreferences` via `SessionManager`
- Compile SDK: 36
- Min SDK: 30

References:
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`

## 3) Personas and Access Model

- `ADMIN`: manages all users and all courses from Data Vault.
- `MENTOR`: manages own courses, views analytics, chats with mentees, checks earnings in profile.
- `MENTEE`: browses/searches courses, opens detail screen, pays/enrolls, chats with mentors.

Role checks are applied at screen level and repository level where needed.

## 4) Authentication and Credentials

### Admin login rule (strict)
- Admin can log in only with:
  - email: `admin`
  - password: `admin`
- This restriction is enforced in:
  - `app/src/main/java/com/example/mad_pop/ui/LoginActivity.java`
  - `app/src/main/java/com/example/mad_pop/data/AuthRepository.java`

### Seed users (auto-created)
- Admin: `admin` / `admin`
- Mentor: `mentor@skillex.com` / `mentor123`
- Mentor: `riya@skillex.com` / `mentor123`
- Mentee: `mentee@skillex.com` / `mentee123`

Seed logic lives in `app/src/main/java/com/example/mad_pop/data/SkillexDbHelper.java`.

## 5) Complete Screen Description (Page by Page)

### `LauncherActivity`
- File: `app/src/main/java/com/example/mad_pop/ui/LauncherActivity.java`
- Layout: `app/src/main/res/layout/activity_launcher.xml`
- Purpose: loading screen with logo + automatic route by current session role.

### `LoginActivity`
- File: `app/src/main/java/com/example/mad_pop/ui/LoginActivity.java`
- Layout: `app/src/main/res/layout/activity_login.xml`
- Purpose: role-based login, email validation, strict admin credential gate.

### `RegisterActivity`
- File: `app/src/main/java/com/example/mad_pop/ui/RegisterActivity.java`
- Layout: `app/src/main/res/layout/activity_register.xml`
- Purpose: create mentor/mentee accounts with input validation.

### `MentorDashboardActivity`
- File: `app/src/main/java/com/example/mad_pop/ui/MentorDashboardActivity.java`
- Layout: `app/src/main/res/layout/activity_mentor_dashboard.xml`
- Purpose: mentor analytics view (courses, enrollments, top course, student list) and action shortcuts.

### `MentorCoursesActivity`
- File: `app/src/main/java/com/example/mad_pop/ui/MentorCoursesActivity.java`
- Layout: `app/src/main/res/layout/activity_mentor_courses.xml`
- Purpose: list mentor-owned courses, open create/edit workflows, manage dates/image key/price.

### `CreateCourseActivity`
- File: `app/src/main/java/com/example/mad_pop/ui/CreateCourseActivity.java`
- Layout: `app/src/main/res/layout/activity_create_course.xml`
- Purpose: create new course with calendar date inputs and price validation.

### `MenteeDashboardActivity`
- File: `app/src/main/java/com/example/mad_pop/ui/MenteeDashboardActivity.java`
- Layout: `app/src/main/res/layout/activity_mentee_dashboard.xml`
- Purpose: browse courses, search courses, chat shortcut, profile shortcut.

### `CourseDetailActivity`
- File: `app/src/main/java/com/example/mad_pop/ui/CourseDetailActivity.java`
- Layout: `app/src/main/res/layout/activity_course_detail.xml`
- Purpose: full course detail page with image, mentor, schedule, description, price, enrollment count, and enroll CTA.

### `PaymentActivity`
- File: `app/src/main/java/com/example/mad_pop/ui/PaymentActivity.java`
- Layout: `app/src/main/res/layout/activity_payment.xml`
- Purpose: payment screen for selected course (GPay QR or credit card details).

### `PaymentConfirmationActivity`
- File: `app/src/main/java/com/example/mad_pop/ui/PaymentConfirmationActivity.java`
- Layout: `app/src/main/res/layout/activity_payment_confirmation.xml`
- Purpose: success state after enrollment + payment transaction.

### `ProfileActivity`
- File: `app/src/main/java/com/example/mad_pop/ui/ProfileActivity.java`
- Layout: `app/src/main/res/layout/activity_profile.xml`
- Purpose: profile editing and logout for mentor/mentee; shows enrolled courses (mentee) or total earnings (mentor).

### `MentorListActivity`, `MentorChatsActivity`, `ChatActivity`
- Files:
  - `app/src/main/java/com/example/mad_pop/ui/MentorListActivity.java`
  - `app/src/main/java/com/example/mad_pop/ui/MentorChatsActivity.java`
  - `app/src/main/java/com/example/mad_pop/ui/ChatActivity.java`
- Layouts:
  - `app/src/main/res/layout/activity_mentor_list.xml`
  - `app/src/main/res/layout/activity_mentor_chats.xml`
  - `app/src/main/res/layout/activity_chat.xml`
- Purpose: mentor-mentee local messaging flow.

### `MentorDataVaultActivity` (Admin panel)
- File: `app/src/main/java/com/example/mad_pop/ui/MentorDataVaultActivity.java`
- Layout: `app/src/main/res/layout/activity_data_vault.xml`
- Purpose: admin management panel for users/courses and data visibility dump.

## 6) Course and Payment Flow

1. Mentee taps a course card in `MenteeDashboardActivity`.
2. App opens `CourseDetailActivity` and loads full course metadata.
3. Mentee taps `Enroll Now`.
4. App opens `PaymentActivity` with course id and price.
5. On pay, `CourseRepository.enrollInCourseWithPayment(...)` performs enrollment + payment insert in one DB transaction.
6. App navigates to `PaymentConfirmationActivity`.
7. Enrolled course appears in mentee profile; mentor earnings update in mentor profile/analytics.

## 7) Database Design

Schema files:
- `app/src/main/java/com/example/mad_pop/data/DbContract.java`
- `app/src/main/java/com/example/mad_pop/data/SkillexDbHelper.java`

Tables:
- `users`: account data + role
- `courses`: title, category, description, mentor, schedule, image key, price
- `enrollments`: mentee-course registration with unique `(user_id, course_id)`
- `messages`: chat data between users
- `payments`: simulated payment records per enrollment

Important integrity constraints:
- role check constraint (`ADMIN`, `MENTOR`, `MENTEE`)
- unique email
- unique enrollment pair
- foreign keys across user/course/message/payment entities

## 8) Repositories and Their Responsibilities

- `AuthRepository`: login/register/profile update, admin account enforcement, user CRUD helpers.
- `CourseRepository`: course CRUD, search, enrollment, payment transaction, mentor analytics, earnings, date and price validation.
- `ChatRepository`: sending and reading messages between users.

## 9) UI, Assets, and Branding

- App name: `Skillex` (`app/src/main/res/values/strings.xml`)
- Launcher icon: manifest uses `@mipmap/ic_launcher` and round icon `@mipmap/ic_launcher_round`.
- Branded logo asset: `app/src/main/res/drawable-nodpi/skillex_logo.png`
- Circular logo background: `app/src/main/res/drawable/bg_logo_circle.xml`
- Motion transitions: `app/src/main/res/anim/slide_in_left.xml`, `app/src/main/res/anim/slide_in_right.xml`, `app/src/main/res/anim/slide_out_left.xml`, `app/src/main/res/anim/slide_out_right.xml`

Course image mapping is handled by key values (for example `demo_uiux`, `uiux`, `demo_python`, `datascience`) and local drawables.

## 10) Key File Map (What Each Folder Does)

- `app/src/main/java/com/example/mad_pop/ui/`: all Activities (screen logic + navigation).
- `app/src/main/java/com/example/mad_pop/data/`: DB schema, helper, and repositories.
- `app/src/main/java/com/example/mad_pop/model/`: plain model classes (`User`, `Course`, `Message`, `MentorAnalytics`).
- `app/src/main/java/com/example/mad_pop/adapter/`: RecyclerView adapters (`CourseAdapter`, `UserAdapter`, `MessageAdapter`).
- `app/src/main/java/com/example/mad_pop/util/`: utilities (`SessionManager`, search helper).
- `app/src/main/res/layout/`: screen and item XML layouts.
- `app/src/main/res/drawable/` and `app/src/main/res/drawable-nodpi/`: visual assets and custom backgrounds.

## 11) Build, Test, and Run

Use these commands from project root:

```powershell
cd C:\Coding\CLONE\MAD_POP
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

Then run the `app` module from Android Studio / JetBrains IDE on an emulator or physical Android device.

## 12) Current Limitations and Future Improvements

- Password storage is plain text (demo scope); should move to secure hashing.
- Payments are simulated; no real payment gateway integration.
- Chat is local DB based (not real-time multi-device messaging).
- No remote sync or cloud backup.

Suggested next upgrades:
- secure auth + encrypted storage,
- backend APIs and token-based sessions,
- real payment provider integration,
- push notifications and real-time chat sockets,
- improved automated UI/instrumentation test coverage.


