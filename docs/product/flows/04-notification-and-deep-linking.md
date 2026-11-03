# Flow 04: Push Notification & Deep Linking

## 1. Overview
Users receive push alerts when new 100% free courses matching their subscribed topics are posted, or when a bookmarked course is close to coupon expiration. Tapping notifications directly navigates them to the target course screen.

---

## 2. Multiplatform Architecture

```mermaid
flowchart TD
    subgraph Multiplatform Push Layer [core:common & domain:user]
        PNM[PushNotificationManager Interface]
        DTR[DeviceTokenRepository]
        NT[NotificationTopic: deals_all, deals_development, deals_business, deals_design, deals_it_software]
    end

    subgraph Android Platform [:app]
        APNM[AndroidPushNotificationManager]
        FCMService[CourseDealsFirebaseMessagingService]
        FCM[Firebase Cloud Messaging SDK]
        MainAct[MainActivity Intent Filter: coursedeals://courses/{id}]
        DetailAct[CouponDetailActivity]
    end

    subgraph iOS Platform [:shared / iosApp]
        IPNM[IosPushNotificationManager]
        MainVC[MainViewController handleDeepLink]
        IOSApp[iOSApp / ContentView onOpenURL]
        AppScaffold[AppScreen.Detail Navigation]
    end

    PNM --> APNM
    PNM --> IPNM
    APNM --> FCM
    FCMService --> DTR
    MainAct -->|coursedeals://courses/:id| DetailAct
    IOSApp --> MainVC
    MainVC --> AppScaffold
```

---

## 3. Notification Dispatch & Deep Link Routing

```mermaid
flowchart TD
    Crawler([Crawler Finds High-Value Free Deal]) --> MatchRules[Match User Notification Subscriptions]
    MatchRules --> PushQueue[Push Notification Service]
    PushQueue --> FCM[Firebase Cloud Messaging / APNs]
    FCM --> Device[User Device Receives Push Notification]

    Device --> ClickPush[User Taps Notification]
    ClickPush --> AppEntry{App Platform?}

    AppEntry -->|Android| MainAct[MainActivity onNewIntent / onCreate]
    AppEntry -->|iOS| IOSOpen[iOS onOpenURL / MainViewController]

    MainAct --> ParseDeepLink[Extract course_id from coursedeals://courses/{id}]
    IOSOpen --> ParseDeepLink

    ParseDeepLink --> RouteChoice{Valid Route?}
    RouteChoice -->|coursedeals://courses/:id| DetailScreen[Course Detail Screen]
    RouteChoice -->|Invalid / Fallback| HomeScreen[Home Screen Feed]
```

---

## 4. Sequence: Device Token Registration & Topic Subscription

```mermaid
sequenceDiagram
    autonumber
    participant App as Mobile App (Android / iOS)
    participant PNM as PushNotificationManager
    participant DTR as DeviceTokenRepository
    participant DTA as DeviceTokenApi
    participant BE as Backend (/api/v1/users/device-token)
    participant FCM as Firebase / APNs

    App->>PNM: Initialize Push Registration
    PNM->>FCM: Fetch FCM Token
    FCM-->>PNM: Device Token (fcmToken)
    PNM->>DTR: saveDeviceToken(token)
    PNM->>DTR: registerDeviceTokenRemote(token, platform)
    DTR->>DTA: POST /api/v1/users/device-token
    DTA->>BE: {"fcmToken": "...", "platform": "ANDROID"|"IOS"}
    BE-->>DTA: 200 OK {"registered": true}
    DTA-->>DTR: NetworkResponse.Success
    App->>PNM: subscribeToTopic("deals_all")
    PNM->>FCM: subscribeToTopic("deals_all")
    PNM->>DTR: saveSubscribedTopics(["deals_all"])
```

---

## 5. Sequence: Push Alert Receipt & Navigation

```mermaid
sequenceDiagram
    autonumber
    participant FCM as Firebase Cloud Messaging
    actor User
    participant Service as CourseDealsFirebaseMessagingService
    participant MainAct as MainActivity / MainViewController
    participant VM as CouponDetailViewModel
    participant BE as Coupon API Service

    FCM->>Service: onMessageReceived(data: { "courseId": "982", "title": "Free Deal!" })
    Service->>User: Display Notification (PendingIntent: coursedeals://courses/982)
    User->>MainAct: Tap Push Notification
    MainAct->>MainAct: Parse deep link URI "coursedeals://courses/982"
    MainAct->>VM: Open Detail Screen (courseId = 982)
    VM->>BE: GET /api/v1/coupons/982
    BE-->>VM: 200 OK (Coupon & Course Details)
    VM-->>User: Render Course Detail Screen directly
```

---

## 6. Key Rules & Topics
* **Supported Topics**:
  - `deals_all`: All 100% discount deals
  - `deals_development`: Development & Programming courses
  - `deals_business`: Business & Finance courses
  - `deals_design`: Design & UI/UX courses
  - `deals_it_software`: IT & Software infrastructure
* **Deep Link Schema**: Supports custom URI scheme `coursedeals://courses/{id}`.
* **Master Switch Hierarchy**: Turning off Master Notification mutes/unsubscribes all topic subscriptions. Re-enabling restores user topics or defaults to `deals_all`.
* **Security & Immutability**: All notification `PendingIntent` instances use `FLAG_IMMUTABLE`. Deep links are parsed strictly with numeric course ID validation.
