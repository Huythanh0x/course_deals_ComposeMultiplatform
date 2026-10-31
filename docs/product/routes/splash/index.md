# Route: Splash Screen (`feature/splash`)

## 1. Overview
The entry point on cold start. Responsible for initializing app dependencies, validating or refreshing authentication credentials, reading dynamic/deep link parameters, and dispatching the user to the correct initial route.

---

## 2. State & Navigation Flow

```mermaid
flowchart TD
    Launch([App Cold Launch]) --> Init[Splash Screen Displayed]
    Init --> CheckAuth[Check Session in DataStore]

    CheckAuth --> HasToken{Has Token?}
    HasToken -->|No| CheckDeepLink[Check Intent / Deep Link]
    HasToken -->|Yes| RefreshToken{Is Token Expired?}

    RefreshToken -->|Valid| CheckDeepLink
    RefreshToken -->|Expired| CallRefresh[POST /api/v1/auth/refresh]

    CallRefresh -->|Refresh 200 OK| SaveNewToken[Save New Token] --> CheckDeepLink
    CallRefresh -->|Refresh 401/Failed| ClearSession[Clear Credentials to Guest] --> CheckDeepLink

    CheckDeepLink --> HasLink{Deep Link Present?}
    HasLink -->|Yes: Target Route| NavTarget[Navigate to Deep Link Route]
    HasLink -->|No| NavHome[Navigate to Home Feed]
```

---

## 3. Screen States
| State | Trigger | UI Presentation |
|---|---|---|
| **Loading** | Initial startup | App branding logo, subtle animated loading indicator. |
| **Error / Network Offline** | Critical startup config failure | Retry button with option to continue offline. |
| **Complete** | Checks finished | Navigates immediately with slide/fade transition to target screen. |

---

## 4. Backend Contracts
* `POST /api/v1/auth/refresh`
  * **Request Header**: `Authorization: Bearer <refreshToken>`
  * **Response (200 OK)**:
    ```json
    {
      "accessToken": "ey...",
      "refreshToken": "ey...",
      "expiresInSeconds": 3600
    }
    ```
