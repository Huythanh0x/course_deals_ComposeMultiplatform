# Comprehensive Test Suite & Use Case Specifications

This document defines exhaustive, branch-complete use case stories across all application screens and features in `course_deals_ComposeMultiplatform` and `course_deals_backend`. It is designed for both **Manual QA Verification** and **Automated Compose UI / Unit Test Coverage** targeting 100% of code branches (`if-else`, `when`, error boundaries, network/cache permutations).

---

## 1. Feature: Splash & Boot Dispatch (`feature/splash`)

### Branches Covered
1. `hasAuthToken == true` AND `isTokenValid == true`
2. `hasAuthToken == true` AND `isTokenExpired == true` -> Refresh 200 OK
3. `hasAuthToken == true` AND `isTokenExpired == true` -> Refresh 401/Fail -> Fallback to Guest
4. `hasAuthToken == false` (First time install / logged out)
5. `incomingDeepLink == ValidCourseUri` (`coursedeals://courses/123`)
6. `incomingDeepLink == MalformedUri`
7. `forceUpdateRequired == true` (`clientVersion < minSupportedVersion`)
8. `networkAvailable == false` (Cold launch offline)

| Test ID | Scenario | Preconditions / Input | Action | Expected Output & State Transition |
|---|---|---|---|---|
| **SPLASH-01** | Happy Path: Existing Authenticated User | Valid `accessToken` & `refreshToken` in DataStore | Cold launch app | Shows Splash logo -> Checks token -> Navigates to `HomeScreen` in authenticated mode. |
| **SPLASH-02** | Expired Token: Auto-Refresh Success | Expired `accessToken`, valid `refreshToken` | Cold launch app | Triggers `POST /api/v1/auth/refresh` -> Returns 200 OK -> Saves new tokens -> Navigates to `HomeScreen`. |
| **SPLASH-03** | Expired Token: Refresh Fails | Revoked or expired `refreshToken` | Cold launch app | `POST /api/v1/auth/refresh` returns 401 -> Clears session credentials -> Reverts to Guest -> Navigates to `HomeScreen`. |
| **SPLASH-04** | Fresh Install / Guest Launch | No stored tokens | Cold launch app | Skips refresh -> Sets session as `isGuest = true` -> Navigates to `HomeScreen`. |
| **SPLASH-05** | Deep Link Dispatch (Active App) | Valid deep link `coursedeals://courses/99` | App opened from external link | Intercepts Intent URI -> Bypasses default home route -> Navigates directly to `DetailScreen(courseId = 99)`. |
| **SPLASH-06** | Malformed Deep Link Fallback | Invalid URI `coursedeals://unknown/foo` | External open | Intercepts Intent -> Validation regex fails -> Logs non-fatal warning -> Navigates gracefully to `HomeScreen`. |
| **SPLASH-07** | Force Update Gate Triggered | Remote config `minSupportedVersionCode = 110`, App version `105` | Cold launch app | Splash detects outdated version -> Halts navigation -> Displays non-dismissible "Update App Required" screen with Play Store CTA. |
| **SPLASH-08** | Offline Cold Launch | Device in Airplane mode, Room DB has cached data | Cold launch app | Network check fails -> Bypasses refresh without crash -> Navigates to `HomeScreen` serving local Room cache. |

---

## 2. Feature: Authentication (`feature/auth`)

### Branches Covered
1. Mode switching: `AuthMode.LOGIN` <-> `AuthMode.REGISTER` <-> `AuthMode.VERIFY_OTP`
2. Email validation: Valid email format vs. Empty vs. Invalid regex
3. Google Sign-In: One-Tap success vs. User cancelled vs. Backend 401 exchange failure
4. OTP Request: 200 OK vs. Rate-limited (429) vs. Offline (503)
5. OTP Verification: 6-digit match (200 OK) vs. Incorrect code (400) vs. Expired code
6. Consent checkbox: Privacy policy checked vs. Unchecked (Block submit)

| Test ID | Scenario | Preconditions / Input | Action | Expected Output & State Transition |
|---|---|---|---|---|
| **AUTH-01** | Email Validation Guard | `LoginScreen` in `LOGIN` mode | Type "invalid-email" and tap Continue | Continue button disabled or shows inline validation error: "Invalid email format". |
| **AUTH-02** | Request OTP Success | Valid email "user@example.com" | Tap Continue | `isLoading = true` -> Backend returns 200 -> State changes to `AuthMode.VERIFY_OTP`, `pendingEmail = "user@example.com"`, countdown timer starts. |
| **AUTH-03** | Request OTP Rate Limited | Same email submitted 3 times within 1 min | Tap Continue | Backend returns 429 -> Displays error banner: "Too many attempts. Please wait 60s." -> Does not switch to OTP screen. |
| **AUTH-04** | Verify OTP Success (Existing User) | On `VERIFY_OTP` screen | Enter correct 6-digit code "123456" | Calls `POST /auth/otp/verify` -> Returns 200 with tokens, `isNewUser = false` -> Stores tokens -> Navigates back / Home. |
| **AUTH-05** | Verify OTP Success (New User Onboarding) | On `VERIFY_OTP` screen | Enter correct code for brand new email | Returns 200 with `isNewUser = true` -> Stores tokens -> Shows welcome toast / navigates to profile interests setup. |
| **AUTH-06** | Verify OTP Incorrect Code | On `VERIFY_OTP` screen | Enter wrong code "999999" | Returns 400 `INVALID_OTP` -> Clears OTP input field -> Displays inline error: "Incorrect verification code. Try again." |
| **AUTH-07** | OTP Resend Cooldown | On `VERIFY_OTP` screen | Tap "Resend Code" while timer > 0s | Button is disabled and displays seconds remaining (`00:42`). |
| **AUTH-08** | Google Sign-In Success | Google Play services available | Tap "Sign in with Google" -> Select Google Account | Native One-Tap returns ID token -> BE exchanges token -> 200 OK -> Navigates to authenticated home. |
| **AUTH-09** | Google Sign-In Cancelled | One-Tap dialog shown | User dismisses dialog / taps outside | App returns to idle state without error dialog or screen freeze. |
| **AUTH-10** | Terms & Privacy Consent Gate | New user registration flow | Attempt to submit with Terms checkbox unchecked | Submit button remains disabled with warning tooltip: "Please accept Terms & Privacy Policy to continue". |

---

## 3. Feature: Home Deal Feed & Search (`feature/home`)

### Branches Covered
1. Initial load: Cache hit vs. Cache miss -> Network fetch
2. Network failure: Offline with Room cache vs. Offline with empty cache
3. Category filter: All deals vs. Single category chip selected vs. Category with 0 deals
4. Search debounce: Typed query < 2 chars vs. >= 3 chars with suggestions
5. Pull-to-refresh: Syncing spinner active -> Updates Room DB -> Emits new list
6. Pagination: Scroll to bottom -> Fetch next page vs. Last page reached (`endOfPaginationReached`)

| Test ID | Scenario | Preconditions / Input | Action | Expected Output & State Transition |
|---|---|---|---|---|
| **HOME-01** | First Load Network Success | Device online, clean install | Open Home Screen | Shows shimmering skeleton cards -> Fetches `GET /coupons` -> Renders feed of deal cards with 100% OFF badge. |
| **HOME-02** | Offline with Existing Room Cache | 20 deals saved in Room DB, Airplane mode enabled | Open Home Screen | Renders cached deal cards instantly -> Top banner shows: "Offline — Showing cached deals from [relative time]". |
| **HOME-03** | Offline with Empty Cache | No cached deals, Airplane mode enabled | Open Home Screen | Displays full-screen Offline illustration with "Retry Connection" CTA. |
| **HOME-04** | Category Filter Selection | Active feed displayed | Tap "Development" category chip | Filter chip highlights -> Feed updates with coupons where `categoryId == DEVELOPMENT`. |
| **HOME-05** | Category Empty State | Select category with zero active coupons | Tap "Photography" | Feed displays EmptyState component: "No free coupons right now for Photography. Check back soon!". |
| **HOME-06** | Search Query Debounce & Suggestions | User in search bar | Type "py" | No network search triggered (threshold < 3 chars). |
| **HOME-07** | Live Search Filter Execution | User in search bar | Type "python" | 300ms debounce fires -> Shows search suggestions matching "python" -> Tapping suggestion filters feed. |
| **HOME-08** | Pull to Refresh | Feed displayed | Swipe down from top of list | `isSyncing = true` -> Fetches page 0 from API -> Upserts Room -> Updates `statFetchedTimestamp` -> Dismisses swipe indicator. |
| **HOME-09** | Endless Scroll Pagination | At bottom of item 20 on page 0 | Scroll past last visible card | Displays bottom loading spinner -> Fetches page 1 -> Appends 20 new items to list smoothly. |
| **HOME-10** | End of Pagination Boundary | At last page (`page == totalPages - 1`) | Scroll to bottom | Bottom loader does not trigger -> No redundant API requests emitted. |

---

## 4. Feature: Course Detail (`feature/detail`)

### Branches Covered
1. Coupon Status: `ACTIVE` vs. `EXPIRED`
2. Source Origin: Direct Network 200 vs. 24h Room Cache fallback
3. Bookmark Action: Authenticated user (Sync to BE) vs. Guest user (Prompt login or store locally)
4. Deal Reporting: Report expired (< threshold) vs. Report expired (>= threshold triggers re-validation)
5. Share: Native OS Share sheet invocation

| Test ID | Scenario | Preconditions / Input | Action | Expected Output & State Transition |
|---|---|---|---|---|
| **DETAIL-01** | Active Coupon View | Valid coupon ID, status `ACTIVE` | Tap course card on Home | Hero image, title, rating, instructor, and price ($0.00) displayed. Sticky bottom button shows "Claim Free Course". |
| **DETAIL-02** | Expired Coupon State | Coupon marked `EXPIRED` on BE | Open detail screen | Sticky bottom button disabled: "Deal Expired" -> Expired alert banner shows with "Find Similar Free Courses" CTA. |
| **DETAIL-03** | Cached Detail Notice | Network unavailable, deal cached < 24h ago | Open detail screen offline | Loads from Room cache -> Displays banner: "Viewing offline copy (Saved [X] hours ago)". |
| **DETAIL-04** | Bookmark Toggle (Authenticated) | User logged in | Tap Heart / Bookmark icon | Icon changes to filled heart -> Optimistically saves to Room -> Fires `PUT /users/me/bookmarks/:id`. |
| **DETAIL-05** | Bookmark Toggle (Guest User) | User unauthenticated | Tap Heart / Bookmark icon | Saves to local Room database -> Shows Toast: "Saved locally. Sign in anytime to sync across devices." |
| **DETAIL-06** | Native Share Invocation | Any detail screen | Tap Share icon | Launches Android/iOS system Share Sheet containing formatted text: "[Course Title] is 100% FREE! [Link]". |
| **DETAIL-07** | Report Expired Deal Modal | Active deal that expired early on Udemy | Tap "Report Broken / Expired" | Opens report dialog -> Select "Coupon is expired" -> Sends `POST /coupons/:id/report` -> Shows "Thank you for reporting!". |

---

## 5. Feature: Coupon Claim & Enrollment (`feature/enroll`)

### Branches Covered
1. Clipboard interaction: System clipboard copy success vs. security exception
2. Redirection: Udemy app installed (Native Intent) vs. No Udemy app (Chrome Custom Tab / Safari View)
3. Telemetry: Anonymous click event vs. Authenticated click event

| Test ID | Scenario | Preconditions / Input | Action | Expected Output & State Transition |
|---|---|---|---|---|
| **ENROLL-01** | Claim Deal Execution | User on Course Detail screen | Tap "Claim Free Course" | BottomSheet confirms claim -> Copies code to device clipboard -> Emits toast "Coupon code copied!" -> Opens external browser to Udemy. |
| **ENROLL-02** | Native Udemy App Preference | Official Udemy app installed on phone | Tap "Claim Free Course" | App checks package manager -> Resolves deep link into native Udemy app checkout screen with coupon pre-filled. |
| **ENROLL-03** | Claim Telemetry Firing | Network active | Tap "Claim Free Course" | Emits non-blocking `POST /api/v1/coupons/:id/claim-events` in background coroutine without delaying URL open. |
| **ENROLL-04** | In-App Review Prompt on 2nd Claim | User has completed 1 previous claim | Completes 2nd coupon claim | Returns from browser -> Google Play In-App Review dialog triggers gracefully. |

---

## 6. Feature: Profile & Preferences (`feature/profile`)

### Branches Covered
1. Identity header: Guest mode vs. Logged in member
2. Dark Mode: System default vs. Force Dark vs. Force Light
3. Push notification categories: Toggle ON/OFF -> Update preferences API
4. Account deletion: Cancel vs. Confirm -> Delete data & cascade wipe
5. Logout: Confirm -> Clear tokens, wipe Room bookmarks, revert to Guest

| Test ID | Scenario | Preconditions / Input | Action | Expected Output & State Transition |
|---|---|---|---|---|
| **PROF-01** | Guest State View | Unauthenticated session | Open Profile tab | Displays Hero card: "Sign In or Register to sync bookmarks and customize deal alerts" with Sign In CTA. |
| **PROF-02** | Authenticated Member View | Logged in user "test@example.com" | Open Profile tab | Shows user email, avatar placeholder, "Saved Bookmarks" count, and "Sign Out" button. |
| **PROF-03** | Dark Mode Toggle | App in Light theme | Toggle "Dark Theme" switch | App theme instantly flips to Compose DarkColorScheme without Activity recreate or state loss. |
| **PROF-04** | Topic Alert Toggle | Push permission granted | Toggle "Development Deals" switch OFF | Updates local preference -> Fires `PUT /users/me/notification-preferences` -> FCM un-subscribes from `deals_development`. |
| **PROF-05** | Logout Cancellation | Logged in user | Tap "Sign Out" -> Tap "Cancel" in confirmation dialog | Dialog closes -> User remains logged in. |
| **PROF-06** | Logout Confirmation | Logged in user | Tap "Sign Out" -> Tap "Confirm Sign Out" | Clears DataStore tokens -> Drops user bookmarks from Room -> Screen smoothly transitions to Guest profile view. |
| **PROF-07** | Account Deletion (GDPR/Store Gate) | Logged in user | Tap "Delete Account" -> Enter confirmation word "DELETE" | Calls `DELETE /api/v1/users/me` -> Backend cascades deletion -> App clears all local data -> Toast: "Account deleted permanently". |

---

## 7. Automated Test Suite Mapping

All test cases map directly into existing unit and UI test structures:

```text
feature/
├── splash/
│   └── SplashScreenTest.kt           # SPLASH-01 to SPLASH-08
├── auth/
│   ├── LoginViewModelTest.kt         # AUTH-01 to AUTH-07
│   └── LoginScreenTest.kt            # AUTH-08 to AUTH-10
├── home/
│   ├── HomeViewModelTest.kt          # HOME-01 to HOME-07
│   └── HomeScreenTest.kt             # HOME-08 to HOME-10
├── detail/
│   ├── CouponDetailViewModelTest.kt  # DETAIL-01 to DETAIL-04
│   └── CouponDetailScreenTest.kt     # DETAIL-05 to DETAIL-07
├── enroll/
│   └── CouponEnrollScreenTest.kt     # ENROLL-01 to ENROLL-04
└── profile/
    ├── ProfileViewModelTest.kt       # PROF-01 to PROF-05
    └── ProfileScreenTest.kt          # PROF-06 to PROF-07
```
