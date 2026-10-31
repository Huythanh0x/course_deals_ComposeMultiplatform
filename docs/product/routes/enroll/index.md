# Route: Enroll & Claim Screen (`feature/enroll`)

## 1. Overview
The terminal action of the coupon redemption funnel. Handles copying coupon codes, firing claim telemetry, and redirecting the user to the course platform (Udemy) via Chrome Custom Tabs, Safari View Controller, or the native Udemy application.

---

## 2. Enrollment Flow

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant App as Mobile App (Enroll BottomSheet)
    participant Clipboard as Device OS Clipboard
    participant BE as Coupon API Service
    participant Browser as Custom Tab / Udemy App

    User->>App: Tap "Claim Deal / Go to Course"
    App->>BE: POST /api/v1/coupons/:id/claim-events
    App->>Clipboard: SetText(couponCode)
    App->>User: Display Toast: "Coupon code copied!"
    App->>Browser: Launch Intent with url=directUrl
    Browser-->>User: Open Provider Landing Page with discount pre-applied
```

---

## 3. Screen Behavior
* Typically presented as an animated **Modal BottomSheet** over the detail view, or directly triggers the intent with a brief confirmation animation.
* Displays a quick instruction: *"We copied the code for you in case it's not applied automatically!"*.
* Includes a **"Report Expired"** secondary button if the user returns to the app and found the code no longer valid on Udemy.

---

## 4. Backend Contracts
* `POST /api/v1/coupons/{id}/claim-events`
  * **Payload**:
    ```json
    {
      "source": "MOBILE_APP",
      "platform": "ANDROID"
    }
    ```
  * **Response (200 OK)**:
    ```json
    { "success": true }
    ```
