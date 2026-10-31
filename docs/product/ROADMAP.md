# Production Roadmap & Feature Backlog

This document catalogs future production-grade features, architectural specifications, and implementation scopes for **Course Deals** across `course_deals_ComposeMultiplatform` and `course_deals_backend`.

---

## 1. Feature Specifications

### 1.1 Firebase Crashlytics & Performance Monitoring
* **Scope**: Mobile Client (`app/build.gradle.kts`)
* **Objective**: Full diagnostic visibility into app crashes and network latency across Android & iOS.
* **Key Architecture**:
  * Add Google Services plugin & Firebase Crashlytics SDK.
  * Custom `CoroutineExceptionHandler` in `core:common` to catch unhandled coroutine faults and log non-fatal exceptions with contextual keys (`currentRoute`, `userId`, `deviceSession`).
  * Firebase Performance network interceptor on HTTP client (measuring latency and payload sizes).

---

### 1.2 Global Error Boundary & Offline Cache Sync (WorkManager)
* **Scope**: Android (`feature:*`, `data:coupons`, WorkManager)
* **Objective**: Offline-first browsing with silent background synchronization.
* **Key Architecture**:
  * Room database (`CouponDatabase`) is already implemented.
  * Add `androidx.work:work-runtime-ktx` with Hilt/Koin worker injection.
  * Implement `PeriodicCouponSyncWorker` running every 6–12 hours on `NetworkType.CONNECTED` + `BatteryNotLow`.
  * Graceful UI banner: "Offline — Showing cached deals from 2 hours ago" when network is unreachable.

---

### 1.3 End-to-End FCM Push Alerts & Topic Subscriptions
* **Scope**: Full-Stack (`modules:notification-service` + Compose App)
* **Objective**: Drive retention with real-time deal alerts and topic notifications.
* **Key Architecture**:
  * **Backend**:
    * New `device_tokens` table mapped to `user_id` (or anonymous device UUID).
    * `POST /api/v1/users/device-token` registration endpoint.
    * Scheduled or event-driven triggers when crawler identifies 100% free courses with rating > 4.5.
  * **Mobile Client**:
    * `FirebaseMessagingService` implementation in `app`.
    * Token synchronization on cold start / token refresh.
    * Topic subscription: `deals_all` or category-specific topics (`deals_development`, `deals_business`).
    * Deep link payload: `{ "route": "coursedeals://courses/{id}" }`.

---

### 1.4 Live Watchers Counter (Lightweight SSE / Redis Pub/Sub)
* **Scope**: Full-Stack (`modules:coupon-api-service` + `feature:detail`)
* **Objective**: Social proof and urgency without heavy bidirectional WebSockets.
* **Key Architecture**:
  * Endpoint: `GET /api/v1/coupons/{id}/live-watchers` (HTTP Server-Sent Events).
  * Backend increments ephemeral Redis key `deal:{id}:viewers` with 30s TTL on client heartbeat.
  * Stream emits viewer count updates: `{ "activeViewers": 18 }`.
  * UI renders animated badge: "🔥 18 people viewing right now". Connection closes when user navigates away.

---

### 1.5 Distributed Client-Side Scraping & Verification (Handshake Protocol)
* **Scope**: Full-Stack (Distributed Crawler Engine)
* **Objective**: Leverage mobile residential IPs to verify Udemy coupon remaining slots without triggering datacenter bot detection.
* **Key Architecture**:
  * **Handshake**: When authenticated user views/claims a deal:
    `POST /api/v1/coupons/{id}/verification-handshake`
  * **Signed Ticket**: BE returns an HMAC-signed short-lived token (JWT, TTL 60s) with `couponId`, `nonce`, and target URL.
  * **Client Fetch**: App queries official Udemy course/pricing endpoint.
  * **Attestation**: App sends back:
    `POST /api/v1/coupons/{id}/attestation`
    Payload: `{ "handshakeToken": "...", "remainingQuantity": 42, "isFree": true, "signature": "..." }`.
  * **Ingestion**: BE validates signature, updates DB remaining count, and rewards user trust score.

---

### 1.6 Scarcity & Countdown Timers
* **Scope**: Mobile Client (`feature:home`, `feature:detail`)
* **Objective**: Visual countdown for deals with close expiration timestamps.
* **Key Architecture**:
  * Compose animated ticker component on cards.
  * Format: "Expires in 02h 15m" or "Last 30 slots remaining".

---

### 1.7 Community Deal Reporting & Status Verification (Issue #100)
* **Scope**: Full-Stack
* **Objective**: Crowdsourced reporting for early expired or broken coupons.
* **Key Architecture**:
  * Endpoint: `POST /api/v1/coupons/{id}/report` (`reason: EXPIRED | PRICE_CHANGED | INVALID`).
  * Backend increments Redis counter `report:{id}:count`.
  * UI displays badge: "Reported 2 times".
  * If report threshold >= 3 within 1 hour, auto-queues high-priority crawler validation job.

---

### 1.8 Backend Full-Text Smart Search & Multi-Filter
* **Scope**: Backend (`modules:coupon-api-service`, `modules:coupon-domain`)
* **Objective**: Support multi-faceted search for Web, iOS, and Android clients.
* **Key Architecture**:
  * Query parameters: `q`, `category`, `minRating`, `language`, `platform`, `freeOnly`, `sort`.
  * Backend implementation via Spring Data JPA Specification or MySQL Full-Text Search on course title and descriptions.

---

### 1.9 In-App Review & Force Update Config (Remote Config)
* **Scope**: Full-Stack / Mobile
* **Objective**: Gracefully prompt for app reviews and enforce minimum client versions for breaking API changes.
* **Key Architecture**:
  * **In-App Review**: Google Play Core In-App Review triggered after user claims 2nd or 3rd deal.
  * **App Config**: `GET /api/v1/app/config` or Firebase Remote Config:
    ```json
    {
      "minSupportedVersion": 105,
      "latestVersion": 106,
      "forceUpdateUrl": "https://play.google.com/store/apps/details?id=com.thanh0x.coursedeals",
      "featureFlags": {
        "clientVerificationEnabled": false,
        "communityReportsEnabled": true
      }
    }
    ```
  * `SplashRoute` blocks navigation with an unskippable update screen if `clientVersion < minSupportedVersion`.

---

### 1.10 Data Privacy & Account Deletion (Store Compliance)
* **Scope**: Full-Stack (Google Play & Apple Store Mandatory)
* **Objective**: GDPR / App Store compliant user account and data deletion.
* **Key Architecture**:
  * Backend: `DELETE /api/v1/users/me` cascading across user records, auth tokens, device tokens, and bookmarks.
  * Mobile: "Delete Account" button in Profile settings with double-confirmation dialog; wipes local DataStore & Room cache.
