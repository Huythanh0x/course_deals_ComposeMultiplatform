import Foundation
import CourseDealsWatch
#if canImport(WidgetKit)
import WidgetKit
#endif

/// Timeline entry representing the current state of Apple Watch Hot Deals complications and Smart Stack.
public struct WatchDealTimelineEntry {
    public let date: Date
    public let topDeal: WatchDealModel?
    public let activeDealCount: Int
    public let bookmarkedCount: Int

    public init(
        date: Date = Date(),
        topDeal: WatchDealModel?,
        activeDealCount: Int,
        bookmarkedCount: Int = 0
    ) {
        self.date = date
        self.topDeal = topDeal
        self.activeDealCount = activeDealCount
        self.bookmarkedCount = bookmarkedCount
    }

    public static func sample(now: Date = Date()) -> WatchDealTimelineEntry {
        let sampleDeal = WatchDealJsonCodec.shared.sampleDeal(
            nowEpochSeconds: Int64(now.timeIntervalSince1970)
        )
        return WatchDealTimelineEntry(
            date: now,
            topDeal: sampleDeal,
            activeDealCount: 5,
            bookmarkedCount: 1
        )
    }
}

#if canImport(WidgetKit)
extension WatchDealTimelineEntry: TimelineEntry {}
#endif

/// Provides timeline entries for WatchKit/WidgetKit complications and watchOS 10+ Smart Stack widgets
/// backed by `WatchDealCacheStore` (App Group `group.com.thanh0x.coursedeals` + `.standard` + shared cache file).
public struct WatchDealTimelineProvider {
    private let cacheStore: WatchDealCacheStore

    public init(cacheStore: WatchDealCacheStore = .shared) {
        self.cacheStore = cacheStore
    }

    public func buildCurrentEntry(now: Date = Date()) -> WatchDealTimelineEntry {
        let topDeal = cacheStore.topActiveDeal(now: now)
        let activeCount = cacheStore.activeDealCount(now: now)
        let bookmarkedCount = cacheStore.loadBookmarkedIds().count
        return WatchDealTimelineEntry(
            date: now,
            topDeal: topDeal,
            activeDealCount: activeCount,
            bookmarkedCount: bookmarkedCount
        )
    }
}

#if canImport(WidgetKit)
extension WatchDealTimelineProvider: TimelineProvider {
    public func placeholder(in context: Context) -> WatchDealTimelineEntry {
        return WatchDealTimelineEntry.sample()
    }

    public func getSnapshot(in context: Context, completion: @escaping (WatchDealTimelineEntry) -> Void) {
        let entry = buildCurrentEntry()
        if entry.topDeal == nil {
            completion(WatchDealTimelineEntry.sample())
        } else {
            completion(entry)
        }
    }

    public func getTimeline(in context: Context, completion: @escaping (Timeline<WatchDealTimelineEntry>) -> Void) {
        let now = Date()
        let entry = buildCurrentEntry(now: now)
        let resolvedEntry = entry.topDeal != nil ? entry : WatchDealTimelineEntry.sample(now: now)
        let nextRefresh = Calendar.current.date(byAdding: .minute, value: 15, to: now)
            ?? now.addingTimeInterval(900)
        let timeline = Timeline(entries: [resolvedEntry], policy: .after(nextRefresh))
        completion(timeline)
    }
}
#endif
