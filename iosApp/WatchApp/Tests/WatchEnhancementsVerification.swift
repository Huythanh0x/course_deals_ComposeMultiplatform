import Foundation
import CourseDealsWatch

#if WATCH_VERIFICATION_MAIN
/// 4-Quadrant Verification Suite for Ticket #233 (#234 & #235):
/// Complications, Smart Stack, Offline Cache, iPhone Handoff, Two-Way Wishlist Sync,
/// and Shared KMP Framework (`CourseDealsWatch.framework`).
public enum WatchEnhancementsVerificationSuite {

    private static func assertCondition(
        _ condition: @autoclosure () -> Bool,
        _ message: String,
        failures: inout [String]
    ) {
        if !condition() {
            failures.append(message)
            print("  ❌ FAIL: \(message)")
        } else {
            print("  ✅ PASS: \(message)")
        }
    }

    @discardableResult
    public static func runAllTests() -> Bool {
        var failures: [String] = []
        let suiteName = "group.com.thanh0x.coursedeals.test.\(UUID().uuidString)"
        let testDefaults = UserDefaults(suiteName: suiteName) ?? .standard
        let store = WatchDealCacheStore(userDefaults: testDefaults, mirrorToStandard: false)
        store.clearAll()

        let now = Date(timeIntervalSince1970: 1_760_000_000)

        print("=== Quadrant 1: Happy Path (Cache Round-Trip, Bookmarks, TimelineEntry, Handoff, KMP CourseDealsWatch.framework) ===")
        let deal1 = WatchDealModel(
            id: 501,
            title: "Kotlin Multiplatform Masterclass",
            heading: nil,
            author: "JetBrains Pro",
            category: "Development",
            couponCode: "KMP100",
            couponUrl: "https://coursedeals.com/deal/501",
            expiredTime: KotlinLong(value: Int64(now.addingTimeInterval(7200).timeIntervalSince1970)),
            rating: KotlinDouble(value: 4.9),
            reviews: KotlinInt(value: 350),
            usesRemaining: KotlinInt(value: 20),
            platform: .udemy,
            isBookmarked: false,
            students: KotlinInt(value: 42_000),
            isHot: true
        )
        let deal2 = WatchDealModel(
            id: 502,
            title: "SwiftUI & WidgetKit for watchOS 10",
            heading: nil,
            author: "Apple Watch Academy",
            category: "Mobile",
            couponCode: "WATCH100",
            couponUrl: "https://coursedeals.com/deal/502",
            expiredTime: KotlinLong(value: Int64(now.addingTimeInterval(3600).timeIntervalSince1970)),
            rating: KotlinDouble(value: 4.8),
            reviews: KotlinInt(value: 210),
            usesRemaining: KotlinInt(value: 12),
            platform: .coursera,
            isBookmarked: false,
            students: KotlinInt(value: 19_000),
            isHot: true
        )

        store.saveDeals([deal1, deal2])
        let loaded = store.loadDeals()
        assertCondition(loaded.count == 2, "Cache round-trip preserves 2 deals", failures: &failures)
        assertCondition(
            deal1.deepLinkUrl.absoluteString == "coursedeals://courses/501",
            "deepLinkUrl formats coursedeals://courses/501",
            failures: &failures
        )

        let rawHotDealsApiJson = """
        {
          "courses": [
            {
              "courseId": 4917622,
              "title": "Personal Growth - Affirmations and Taking Action",
              "author": "Growth Lab",
              "category": "Personal Development",
              "couponCode": "27THOCT26",
              "targetUrl": "https://coursedeals.com/deal/4917622",
              "previewImage": "https://img-c.udemycdn.com/course/480x270/4917622.jpg",
              "expiredTime": 1762500000,
              "rating": 4.9,
              "reviews": 400,
              "students": 28500
            }
          ]
        }
        """.data(using: .utf8)!
        let decodedApiDeals = WatchHotDealsEnvelope.decodeDeals(from: rawHotDealsApiJson)
        assertCondition(
            decodedApiDeals.count == 1 &&
            decodedApiDeals.first?.id == 4917622 &&
            decodedApiDeals.first?.couponCode == "27THOCT26" &&
            decodedApiDeals.first?.platform == .udemy,
            "WatchHotDealsEnvelope.decodeDeals decodes raw /api/v1/coupons/hot-deals JSON via KMP WatchDealJsonCodec",
            failures: &failures
        )

        let updatedBookmarks = store.setBookmarked(dealId: 502, isBookmarked: true)
        assertCondition(updatedBookmarks.contains(502), "setBookmarked stores dealId 502", failures: &failures)
        let reloadedAfterBookmark = store.loadDeals()
        assertCondition(
            reloadedAfterBookmark.first(where: { $0.id == 502 })?.isBookmarked == true,
            "loadDeals applies persisted bookmark flag to deal 502",
            failures: &failures
        )

        let topDeal = store.topActiveDeal(now: now)
        assertCondition(
            topDeal?.id == 501,
            "topActiveDeal selects highest-rated active hot deal (501 with 4.9 rating)",
            failures: &failures
        )

        let provider = WatchDealTimelineProvider(cacheStore: store)
        let entry = provider.buildCurrentEntry(now: now)
        assertCondition(entry.activeDealCount == 2, "Timeline entry reports 2 active deals", failures: &failures)
        assertCondition(entry.bookmarkedCount == 1, "Timeline entry reports 1 bookmarked deal", failures: &failures)
        assertCondition(entry.topDeal?.id == 501, "Timeline entry includes topDeal 501", failures: &failures)

        let handoffInfo = WatchDealDetailView.buildHandoffUserInfo(for: deal1)
        assertCondition(
            WatchDealDetailView.handoffActivityType == "com.thanh0x.coursedeals.courseDetail",
            "Handoff activity type matches com.thanh0x.coursedeals.courseDetail",
            failures: &failures
        )
        assertCondition(
            (handoffInfo["courseId"] as? Int) == 501 &&
            (handoffInfo["deepLink"] as? String) == "coursedeals://courses/501",
            "Handoff userInfo contains courseId=501 and deepLink=coursedeals://courses/501",
            failures: &failures
        )

        print("=== Quadrant 2: Failed Path (Empty Cache & Expired Deals Filtering) ===")
        store.clearAll()
        assertCondition(store.loadDeals().isEmpty, "Cleared cache returns empty array", failures: &failures)
        assertCondition(store.activeDealCount(now: now) == 0, "Cleared cache activeDealCount == 0", failures: &failures)
        assertCondition(store.topActiveDeal(now: now) == nil, "Cleared cache topActiveDeal == nil", failures: &failures)

        let expiredHighRatedDeal = WatchDealModel(
            id: 599,
            title: "Expired 5.0 Rated Course",
            heading: nil,
            author: nil,
            category: nil,
            couponCode: nil,
            couponUrl: nil,
            expiredTime: KotlinLong(value: Int64(now.addingTimeInterval(-3600).timeIntervalSince1970)),
            rating: KotlinDouble(value: 5.0),
            reviews: nil,
            usesRemaining: nil,
            platform: .udemy,
            isBookmarked: false,
            students: nil,
            isHot: true
        )
        store.saveDeals([expiredHighRatedDeal, deal2])
        assertCondition(
            store.activeDealCount(now: now) == 1,
            "activeDealCount excludes expired deal 599 and counts only active deal 502",
            failures: &failures
        )
        assertCondition(
            store.topActiveDeal(now: now)?.id == 502,
            "topActiveDeal skips expired 5.0 deal and selects active deal 502",
            failures: &failures
        )

        print("=== Quadrant 3: Error Path (Corrupt Cache JSON & Invalid Bytes Recovery) ===")
        let corruptBytes = Data([0xFF, 0xFE, 0x00, 0x7B, 0x22, 0x62, 0x72, 0x6F, 0x6B, 0x65, 0x6E])
        testDefaults.set(corruptBytes, forKey: WatchDealCacheStore.cachedDealsKey)
        let recoveredDeals = store.loadDeals()
        assertCondition(
            recoveredDeals.isEmpty,
            "Corrupt bytes in cached_watch_deals_v1 recover gracefully to [] without crashing",
            failures: &failures
        )

        print("=== Quadrant 4: Regression & Related Changes (Bookmark Retention Across Live Refresh) ===")
        store.clearAll()
        store.setBookmarked(dealId: 501, isBookmarked: true)
        let freshUnbookmarkedFeed = WatchDealJsonCodec.shared.decodeDealsFromJson(
            jsonString: """
            [
              {"id": 501, "title": "Refreshed Course 501", "isBookmarked": false},
              {"id": 503, "title": "Refreshed Course 503", "isBookmarked": false}
            ]
            """,
            limit: 10
        )
        let merged = store.mergeDealsWithBookmarks(freshUnbookmarkedFeed, extraBookmarkedIds: [503])
        assertCondition(
            merged.first(where: { $0.id == 501 })?.isBookmarked == true &&
            merged.first(where: { $0.id == 503 })?.isBookmarked == true,
            "mergeDealsWithBookmarks retains local bookmark 501 and merges iPhone remote bookmark 503",
            failures: &failures
        )

        store.clearAll()
        testDefaults.removePersistentDomain(forName: suiteName)

        if failures.isEmpty {
            print("🎉 ALL 4 QUADRANTS PASSED (0 failures)")
            return true
        } else {
            print("❌ VERIFICATION FAILED with \(failures.count) error(s): \(failures)")
            return false
        }
    }
}
#endif
