# Course Deals — Product Overview & Navigation Architecture

## 1. Product Vision
**Course Deals** is a multiplatform application (Android / Compose Multiplatform & Spring Boot backend) designed to aggregate, curate, and distribute verified 100% free and heavily discounted online course coupons (Udemy, Coursera, etc.) in real time.

---

## 2. Core Personas
1. **The Bargain Learner (Guest / Registered)**:
   - Wants to quickly browse active 100% free discount coupons across categories (Development, Business, Design, etc.).
   - Needs frictionless one-tap coupon copying and deep-linking into Udemy/Course landing pages.
2. **The Power Upskiller (Registered User)**:
   - Sets up category or keyword-based deal alerts and push notifications.
   - Saves/bookmarks high-value coupons for later offline review or enrollment.
3. **The Administrator / Crawler Service (System)**:
   - Scrapes, validates, and expires deals continuously to minimize dead links and false-positive expired coupons.

---

## 3. Global Information Architecture & Navigation

```mermaid
flowchart TD
    ColdStart([App Launch / Cold Start]) --> SplashRoute[Splash Route]
    
    SplashRoute -->|Check Session / Auth Token| AuthCheck{Authenticated?}
    SplashRoute -->|Incoming Deep Link| DeepLinkDispatch{Has Deep Link?}

    AuthCheck -->|Valid Token| HomeRoute[Home Screen / Feed]
    AuthCheck -->|No Session / Guest| HomeRoute

    DeepLinkDispatch -->|Valid /courses/:id| DetailRoute[Course Detail Screen]
    DeepLinkDispatch -->|None| HomeRoute

    subgraph MainAppNavigation [Main App Navigation]
        HomeRoute -->|Tap Deal Card| DetailRoute
        HomeRoute -->|Filter / Search| HomeRoute
        HomeRoute -->|Profile Tab| ProfileRoute[Profile / Settings]
        HomeRoute -->|Auth Required Action| AuthRoute[Auth Route]

        DetailRoute -->|Tap Claim / Enroll| EnrollRoute[Enrollment / Web Redirect]
        DetailRoute -->|Tap Bookmark (Guest)| AuthRoute
        DetailRoute -->|Back| HomeRoute

        EnrollRoute -->|External Browser/Udemy App| ExternalUdemy([Udemy Course Page])
        EnrollRoute -->|Back| DetailRoute

        ProfileRoute -->|Login / Signup| AuthRoute
        ProfileRoute -->|Manage Notifications| NotificationSettings[Deal Alerts & Categories]
        ProfileRoute -->|View Bookmarks| SavedDealsList[Saved Coupons]
    end

    subgraph AuthNavigation [Authentication Flow]
        AuthRoute -->|Magic Link / OTP| OTPVerify[Verify OTP Screen]
        AuthRoute -->|Google / Social Sign-In| OAuthCallback[OAuth Handler]
        OTPVerify -->|Success| HomeRoute
        OAuthCallback -->|Success| HomeRoute
    end
```

---

## 4. Documentation Directory Index

### A. End-to-End User Journeys (`flows/`)
* [01. Guest Discovery to Enrollment](flows/01-guest-discovery-to-enroll.md): Frictionless browsing and deal redemption without immediate sign-in.
* [02. Authentication & Account Lifecycle](flows/02-auth-and-account-lifecycle.md): Passwordless OTP, social sign-in, session refresh, and logout.
* [03. Coupon Claim & Redemption](flows/03-coupon-claim-and-redemption.md): Direct checkout, coupon expiration checks, link generation, and coupon code copying.
* [04. Notification & Deep Linking](flows/04-notification-and-deep-linking.md): Real-time deal alert pushes, deep link dispatching, and background wakeups.

### B. Route & Screen Specifications (`routes/`)
* [Splash Route](routes/splash/index.md): Cold start, token refresh, dynamic link handling.
* [Auth Route](routes/auth/index.md): Magic link, OTP verification, Google OAuth, session storage.
* [Home Route](routes/home/index.md): Deal feed, category carousels, sorting, pull-to-refresh.
* [Course Detail Route](routes/course-detail/index.md): Course summary, coupon state (active/expired), bookmarks.
* [Enroll Route](routes/enroll/index.md): Coupon copy modal, tracking, external browser redirect.
* [Profile Route](routes/profile/index.md): Saved bookmarks, deal alert settings, account logout.
