# Flow 04: Push Notification & Deep Linking

## 1. Overview
Users receive push alerts when new 100% free courses matching their subscribed topics are posted, or when a bookmarked course is close to coupon expiration. Tapping notifications directly navigates them to the target course screen.

---

## 2. Notification Dispatch & Deep Link Routing

```mermaid
flowchart TD
    Crawler([Crawler Finds High-Value Free Deal]) --> MatchRules[Match User Notification Subscriptions]
    MatchRules --> PushQueue[Push Notification Service]
    PushQueue --> FCM[Firebase Cloud Messaging / APNs]
    FCM --> Device[User Device Receives Push Notification]

    Device --> ClickPush[User Taps Notification]
    ClickPush --> AppEntry{App State?}

    AppEntry -->|Cold Boot| Splash[Splash Screen / DeepLinkHandler]
    AppEntry -->|Foreground / Background| NavStack[NavController Dispatch]

    Splash --> ParseDeepLink[Extract course_id or route]
    NavStack --> ParseDeepLink

    ParseDeepLink --> RouteChoice{Valid Route?}
    RouteChoice -->|course_deals://course/:id| DetailScreen[Course Detail Screen]
    RouteChoice -->|course_deals://category/:cat| FilteredFeed[Home Feed with Filter]
    RouteChoice -->|Invalid / Fallback| HomeScreen[Home Screen Feed]
```

---

## 3. Sequence: Push Alert Receipt & Navigation

```mermaid
sequenceDiagram
    autonumber
    participant FCM as Firebase / APNs
    actor User
    participant App as Mobile App
    participant VM as CourseDetailViewModel
    participant BE as Coupon API Service

    FCM->>User: Push Alert: "Free Kotlin Multiplatform Course available!"
    User->>App: Tap Push Notification
    Note over User,App: Payload: { "deepLink": "coursedeals://courses/982" }
    App->>App: Intercept Deep Link URI
    App->>App: Navigate to DetailRoute(courseId = 982)
    App->>VM: loadCourse(982)
    VM->>BE: GET /api/v1/coupons/982
    BE-->>VM: 200 OK (Course Details)
    VM-->>App: Emit Success State
    App-->>User: Render Course Detail Screen directly
```

---

## 4. Key Rules
* **Deep Link Schema**: Supports custom URI scheme `coursedeals://` and standard App Links / Universal Links `https://coursedeals.app/courses/{id}`.
* **Cold-Start Resilience**: When opening from a dead app state, the app loads Splash to verify credentials without losing the pending deep link backstack.
* **Notification Preferences**: Users can toggle push notifications per category in their profile settings.
