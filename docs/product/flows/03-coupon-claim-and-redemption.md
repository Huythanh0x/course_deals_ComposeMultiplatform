# Flow 03: Coupon Claim & Redemption Lifecycle

## 1. Overview
Coupons on online course platforms (especially Udemy) have volatile lifecycles: they are limited by duration (typically 3 to 5 days) or redemption quantity caps (e.g. max 1,000 redemptions). This flow defines how the app validates, claims, and updates coupon states.

---

## 2. Coupon State Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Discovered: Crawler Scrapes Deal
    Discovered --> Verified: Validation Worker Confirms Active
    Verified --> Expired: Expiration Date Passed
    Verified --> Expired: Redemption Limit Exceeded (400 from Course Platform)
    Verified --> Claimed: User Taps 'Claim Coupon'
    Claimed --> Verified: External Browser Launched
    Expired --> Archived: Cleanup Service runs
    Archived --> [*]
```

---

## 3. Sequence: Claiming & Reporting Deal Status

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant App as Mobile App
    participant BE as Coupon Domain / API
    participant Scraper as Background Crawler Worker

    User->>App: Clicks 'Claim Coupon' on Course Detail
    App->>BE: POST /api/v1/coupons/{id}/claim (User/Guest click telemetry)
    BE-->>App: 200 OK (Coupon code & target URL)
    App->>App: Copy Coupon Code into Clipboard
    App->>User: Open URL in Chrome Custom Tab / Safari View

    alt Course Is Free
        User->>User: Complete enrollment on Udemy
    else Coupon Expired Early on Provider
        User->>App: Return to App & Click "Report Expired"
        App->>BE: POST /api/v1/coupons/{id}/report-expired
        BE-->>App: 200 OK (Thank you for reporting)
        BE->>Scraper: Queue immediate re-check task
        Scraper->>BE: Update status to EXPIRED if verification fails
    end
```

---

## 4. Key Rules
* **Smart URL Formatting**: If the course already embeds the coupon code in the URL (`?couponCode=FREE100`), the link directly applies the discount.
* **Redundant Clipboard Copy**: The raw coupon code is always placed into the device clipboard so that users can paste it manually if the web redirect drops query parameters.
* **Community Feedback Loop**: Users can flag dead/expired deals, which lowers deal trust score and triggers automatic re-validation by the crawler daemon.
