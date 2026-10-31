# Flow 01: Guest Discovery to Enrollment

## 1. Overview
The primary conversion funnel in Course Deals allows unauthenticated guests to immediately discover, filter, and redeem free course coupons without mandatory sign-in barriers.

---

## 2. User Journey Flowchart

```mermaid
flowchart TD
    A([User Opens App]) --> B[Home Feed Screen]
    B --> C{Browse & Filter}
    C -->|Select Category| D[Filter List by Topic]
    C -->|Search Keyword| E[Query Course Deals]
    C -->|Scroll Feed| F[Paginated Deal Cards]

    D --> G[Tap Course Card]
    E --> G
    F --> G

    G --> H[Course Detail Screen]
    H --> I{Check Coupon Availability}
    
    I -->|Active 100% Off| J[Display Claim Free Course CTA]
    I -->|Expired / Claim Limit Reached| K[Display Expired Badge & Suggest Similar Deals]

    J --> L[Tap 'Claim Deal']
    L --> M[Enrollment Sheet / Direct Link]
    M --> N[Copy Coupon Code to Clipboard]
    N --> O[Open Course URL in External Browser / Udemy App]
    O --> P([User Enrolls on Platform])
```

---

## 3. Sequence Diagram: Interaction Across Layers

```mermaid
sequenceDiagram
    autonumber
    actor User as Guest User
    participant App as Compose Multiplatform App
    participant BE as Coupon API Service
    participant Browser as External Browser / Udemy

    User->>App: Launch app
    App->>BE: GET /api/v1/coupons?status=ACTIVE&page=0
    BE-->>App: 200 OK (Paginated Coupon Cards)
    App-->>User: Render Home Feed

    User->>App: Tap on Course Card (#123)
    App->>BE: GET /api/v1/coupons/123
    BE-->>App: 200 OK (Course Details, Coupon Code, Target URL)
    App-->>User: Display Course Detail

    User->>App: Tap 'Claim Deal'
    App->>BE: POST /api/v1/coupons/123/claim-events
    Note over App,BE: Tracks conversion telemetry anonymously
    App->>App: Copy coupon code to system clipboard
    App->>Browser: Launch Custom Tab or Intent (Udemy URL + Coupon query)
    Browser-->>User: Load Udemy Checkout (100% Free Applied)
```

---

## 4. Key Business Invariants & Edge Cases
* **Zero Friction**: Guests do not need to register to claim coupon links.
* **Expired Status Guard**: If a coupon expired while the user was viewing the screen, clicking 'Claim Deal' triggers a fresh check or displays a warning banner.
* **Clipboard Notification**: App shows a non-intrusive toast: *"Coupon code copied to clipboard!"*.
