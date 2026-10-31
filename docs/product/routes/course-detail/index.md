# Route: Course Detail Screen (`feature/detail`)

## 1. Overview
Displays in-depth course information (syllabus highlights, rating, expiration countdown, provider platform) and acts as the gateway to claiming the coupon.

---

## 2. Interaction & State Flow

```mermaid
flowchart TD
    InitDetail([Course Detail Opened with id]) --> FetchDetail[GET /api/v1/coupons/:id]
    
    FetchDetail --> CheckStatus{Coupon Status}
    CheckStatus -->|ACTIVE| RenderActive[Show 'Claim Free Course' Primary Button]
    CheckStatus -->|EXPIRED| RenderExpired[Show 'Deal Expired' Banner & Similar Courses]

    RenderActive --> UserClick{User Taps}
    UserClick -->|Bookmark Icon| ToggleBookmark[Save to Local & Sync BE]
    UserClick -->|Share Icon| NativeShare[Open Native Share Sheet with URL]
    UserClick -->|Claim Button| OpenEnroll[Navigate to Enroll Route / Modal]
```

---

## 3. Screen Components
1. **Course Hero Header**: Cover image, platform badge (e.g. Udemy), course title, instructor name, and star rating.
2. **Pricing & Status Bar**: Original vs. Discounted price (`$0.00 FREE`), countdown pill (*"Ends in 2 days"*).
3. **Action Bar**:
   - `Bookmark` (Heart/Star icon)
   - `Share`
   - `Claim Coupon` (Sticky bottom CTA)
4. **Course Description & Learnings**: Expandable markdown/text summary.

---

## 4. Backend Contracts
* `GET /api/v1/coupons/{id}`
  * **Response (200 OK)**:
    ```json
    {
      "id": "c_101",
      "title": "Mastering Kotlin Coroutines & Flow",
      "description": "Learn asynchronous programming in Kotlin...",
      "code": "NOVFREEKOTLIN",
      "directUrl": "https://www.udemy.com/course/kotlin-coroutines/?couponCode=NOVFREEKOTLIN",
      "platform": "UDEMY",
      "status": "ACTIVE",
      "expiresAt": "2026-11-03T12:00:00Z"
    }
    ```
