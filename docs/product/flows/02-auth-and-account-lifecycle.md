# Flow 02: Authentication & Account Lifecycle

## 1. Overview
Course Deals uses a modern, lightweight authentication model: **Passwordless Email OTP** and **Social OAuth (Google)**. Users authenticate when they want to unlock saved bookmarks, sync preferences across devices, or configure push notifications.

---

## 2. Authentication State Transition

```mermaid
stateDiagram-v2
    [*] --> Guest: First Launch
    Guest --> AuthScreen: Tap Bookmark / Sign In from Profile
    
    state AuthScreen {
        [*] --> InputEmail: Enter Email
        InputEmail --> RequestingOTP: Tap Send OTP
        RequestingOTP --> EnterOTP: OTP Email Dispatched
        EnterOTP --> ValidatingOTP: User enters 6-digit code
        ValidatingOTP --> AuthSuccess: 200 OK (Tokens received)
        ValidatingOTP --> EnterOTP: Invalid Code (Retry)
    }

    AuthScreen --> Authenticated: AuthSuccess
    Authenticated --> TokenRefreshed: Auto Refresh on 401
    Authenticated --> Guest: User Taps Logout
    TokenRefreshed --> Authenticated: Refresh Succeeded
    TokenRefreshed --> Guest: Refresh Token Expired (401/403)
```

---

## 3. Sequence: Passwordless OTP Authentication Flow

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant App as Compose Multiplatform App
    participant IS as Identity Service (Backend)
    participant Mail as Email / Mail Service

    User->>App: Enter email (user@example.com) & tap "Continue"
    App->>IS: POST /api/v1/auth/otp/request { "email": "user@example.com" }
    IS->>Mail: Generate 6-digit OTP & send email
    Mail-->>User: Receive OTP code
    IS-->>App: 200 OK { "expiresInSeconds": 300 }
    App-->>User: Show OTP input screen with countdown

    User->>App: Input 6-digit OTP code
    App->>IS: POST /api/v1/auth/otp/verify { "email": "user@example.com", "code": "849201" }
    
    alt Valid OTP
        IS-->>App: 200 OK { "accessToken": "...", "refreshToken": "...", "isNewUser": true }
        App->>App: Securely store tokens in Encrypted DataStore / Keychain
        App-->>User: Navigate to previous screen / Home
    else Invalid / Expired Code
        IS-->>App: 400 Bad Request { "code": "INVALID_OTP", "message": "Code is incorrect or expired" }
        App-->>User: Show error message & allow resend
    end
```

---

## 4. Key Security & Business Rules
* **Token Storage**: `accessToken` and `refreshToken` are stored in platform-secure storage (`EncryptedSharedPreferences` on Android, `Keychain` on iOS).
* **Automatic Silent Refresh**: On any `401 Unauthorized` API response, an Authenticator interceptor triggers `POST /api/v1/auth/refresh` before retrying the original request.
* **Graceful Fallback**: If token refresh fails, clear credentials and downgrade user session to `Guest` without crashing.
