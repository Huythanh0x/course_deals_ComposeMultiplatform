# Route: Auth Screen (`feature/auth`)

## 1. Overview
Handles user authentication and account creation via passwordless email OTP and Google OAuth. Users can sign in from the Profile tab or when triggering an authenticated-only action (such as saving a bookmark or customizing push alerts).

---

## 2. Interaction Flow

```mermaid
flowchart TD
    OpenAuth([Open Auth Screen]) --> AuthOptions{Select Auth Method}

    AuthOptions -->|Google Sign-In| GoogleAuth[Trigger Google One-Tap / OAuth]
    GoogleAuth --> BackendGoogle[POST /api/v1/auth/google { idToken }]
    BackendGoogle --> AuthSuccess[Save Session & Return]

    AuthOptions -->|Email OTP| EnterEmail[Input Email Address]
    EnterEmail --> TapSend[Tap 'Continue']
    TapSend --> RequestOTP[POST /api/v1/auth/otp/request]
    
    RequestOTP --> ShowOTP[Display 6-Digit OTP Input & Countdown]
    ShowOTP --> EnterCode[User Enters Code]
    EnterCode --> VerifyOTP[POST /api/v1/auth/otp/verify]

    VerifyOTP -->|Success| AuthSuccess
    VerifyOTP -->|Invalid Code| ShowError[Display 'Incorrect Code' Inline Error]
    ShowError --> ShowOTP
```

---

## 3. Screen States & UI Elements
* **Input State**: Clean input field with email validation regex and Google Sign-In button.
* **OTP Challenge State**: 6-box pin input with autofocus, 60-second resend cooldown timer.
* **Loading State**: Button spinners disable re-submitting while network requests are in-flight.
* **Error Banner**: Human-readable messages (e.g., *"Rate limit exceeded. Please wait 1 minute."*).

---

## 4. Backend Contracts
* `POST /api/v1/auth/otp/request`
  ```json
  { "email": "user@example.com" }
  ```
* `POST /api/v1/auth/otp/verify`
  ```json
  { "email": "user@example.com", "code": "123456" }
  ```
  * **Response (200 OK)**:
    ```json
    {
      "accessToken": "ey...",
      "refreshToken": "ey...",
      "isNewUser": false,
      "user": {
        "id": "usr_99182",
        "email": "user@example.com"
      }
    }
    ```
