import Foundation
import SwiftUI
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
        let sampleDeal = WatchDealModel(
            id: 501,
            title: "Complete Kotlin Multiplatform & SwiftUI Masterclass",
            author: "Course Deals Academy",
            category: "Mobile Development",
            couponCode: "KMP100FREE",
            couponUrl: "https://coursedeals.com/deal/501",
            expiredTime: Int64(now.addingTimeInterval(7200).timeIntervalSince1970),
            rating: 4.9,
            reviews: 420,
            usesRemaining: 18,
            platform: .udemy,
            isBookmarked: true,
            students: 48_500,
            isHot: true
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
/// backed by `WatchDealCacheStore` (App Group `group.com.thanh0x.coursedeals` + `.standard` fallback).
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
        if context.isPreview && entry.topDeal == nil {
            completion(WatchDealTimelineEntry.sample())
        } else {
            completion(entry)
        }
    }

    public func getTimeline(in context: Context, completion: @escaping (Timeline<WatchDealTimelineEntry>) -> Void) {
        let now = Date()
        let entry = buildCurrentEntry(now: now)
        let nextRefresh = Calendar.current.date(byAdding: .minute, value: 30, to: now)
            ?? now.addingTimeInterval(1800)
        let timeline = Timeline(entries: [entry], policy: .after(nextRefresh))
        completion(timeline)
    }
}
#endif

// MARK: - Complication Family Views

/// `.accessoryCircular` complication showing active hot deal count inside a circular gauge badge.
public struct CircularHotDealsComplicationView: View {
    public let activeDealCount: Int

    public init(activeDealCount: Int) {
        self.activeDealCount = activeDealCount
    }

    public var body: some View {
        ZStack {
            Circle()
                .strokeBorder(
                    LinearGradient(
                        colors: [.orange, .red],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 3.5
                )
                .background(Circle().fill(Color.orange.opacity(0.15)))

            VStack(spacing: 0) {
                Image(systemName: "flame.fill")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.orange)
                Text("\(activeDealCount)")
                    .font(.system(size: 14, weight: .heavy, design: .rounded))
                    .foregroundColor(.white)
                Text("HOT")
                    .font(.system(size: 7, weight: .bold))
                    .foregroundColor(.secondary)
            }
        }
        .frame(width: 46, height: 46)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(activeDealCount) active hot deals")
    }
}

/// `.accessoryRectangular` complication showing top hot deal title, platform, rating, and countdown.
public struct RectangularHotDealComplicationView: View {
    public let deal: WatchDealModel?
    public let activeDealCount: Int
    public let now: Date

    public init(deal: WatchDealModel?, activeDealCount: Int, now: Date = Date()) {
        self.deal = deal
        self.activeDealCount = activeDealCount
        self.now = now
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            if let deal = deal {
                HStack(spacing: 4) {
                    Text("🔥 \(deal.platform.displayName.uppercased())")
                        .font(.system(size: 9, weight: .heavy))
                        .foregroundColor(.orange)
                    Spacer()
                    Text(deal.discountBadgeText)
                        .font(.system(size: 8, weight: .heavy))
                        .padding(.horizontal, 4)
                        .padding(.vertical, 1)
                        .background(Color.green.opacity(0.25))
                        .foregroundColor(.green)
                        .cornerRadius(3)
                }

                Text(deal.title)
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(.white)
                    .lineLimit(1)

                HStack(spacing: 5) {
                    Text(deal.formattedRating)
                        .font(.system(size: 9, weight: .bold))
                        .foregroundColor(.yellow)
                    Text("•")
                        .font(.system(size: 9))
                        .foregroundColor(.secondary)
                    Text(deal.formattedTimeLeft(now: now))
                        .font(.system(size: 9, weight: .medium, design: .monospaced))
                        .foregroundColor(.cyan)
                    Spacer()
                    if activeDealCount > 1 {
                        Text("+\(activeDealCount - 1) more")
                            .font(.system(size: 8))
                            .foregroundColor(.secondary)
                    }
                }
            } else {
                HStack(spacing: 4) {
                    Image(systemName: "flame.fill")
                        .foregroundColor(.orange)
                    Text("Course Deals")
                        .font(.system(size: 10, weight: .bold))
                }
                Text("No active hot deals cached")
                    .font(.system(size: 10))
                    .foregroundColor(.secondary)
                Text("Tap to refresh 100% OFF coupons")
                    .font(.system(size: 8))
                    .foregroundColor(.cyan)
            }
        }
        .padding(6)
        .background(Color.white.opacity(0.08))
        .cornerRadius(8)
    }
}

/// `.accessoryInline` complication rendering a single-line summary for watch faces.
public struct InlineHotDealComplicationView: View {
    public let deal: WatchDealModel?
    public let activeDealCount: Int
    public let now: Date

    public init(deal: WatchDealModel?, activeDealCount: Int, now: Date = Date()) {
        self.deal = deal
        self.activeDealCount = activeDealCount
        self.now = now
    }

    public var summaryText: String {
        if let deal = deal {
            return "🔥 \( max(1, activeDealCount) ) Hot Deals • \(deal.formattedRating) • \(deal.formattedTimeLeft(now: now))"
        }
        return "🔥 Course Deals • Tap for 100% OFF"
    }

    public var body: some View {
        Text(summaryText)
            .font(.system(size: 10, weight: .semibold))
            .lineLimit(1)
    }
}

/// `.accessoryCorner` complication rendering deal count with corner badge.
public struct CornerHotDealsComplicationView: View {
    public let activeDealCount: Int

    public init(activeDealCount: Int) {
        self.activeDealCount = activeDealCount
    }

    public var body: some View {
        HStack(spacing: 3) {
            Image(systemName: "flame.fill")
                .font(.system(size: 11, weight: .bold))
                .foregroundColor(.orange)
            Text("\(activeDealCount) FREE")
                .font(.system(size: 10, weight: .heavy, design: .rounded))
                .foregroundColor(.green)
        }
    }
}

#if canImport(WidgetKit) && (os(watchOS) || os(iOS))
/// Adaptive WidgetKit entry view switching across watchOS complication families.
public struct HotDealsComplicationEntryView: View {
    @Environment(\.widgetFamily) private var widgetFamily
    public let entry: WatchDealTimelineEntry

    public init(entry: WatchDealTimelineEntry) {
        self.entry = entry
    }

    public var body: some View {
        Group {
            switch widgetFamily {
            case .accessoryCircular:
                CircularHotDealsComplicationView(activeDealCount: entry.activeDealCount)
            case .accessoryRectangular:
                RectangularHotDealComplicationView(
                    deal: entry.topDeal,
                    activeDealCount: entry.activeDealCount,
                    now: entry.date
                )
            case .accessoryInline:
                InlineHotDealComplicationView(
                    deal: entry.topDeal,
                    activeDealCount: entry.activeDealCount,
                    now: entry.date
                )
            #if os(watchOS)
            case .accessoryCorner:
                CornerHotDealsComplicationView(activeDealCount: entry.activeDealCount)
            #endif
            default:
                RectangularHotDealComplicationView(
                    deal: entry.topDeal,
                    activeDealCount: entry.activeDealCount,
                    now: entry.date
                )
            }
        }
        .widgetURL(entry.topDeal?.deepLinkUrl)
    }
}

/// WidgetKit configuration for watchOS Watch Face Complications.
public struct HotDealsComplicationWidget: Widget {
    public static let kind = "com.thanh0x.coursedeals.watch.complication"

    public init() {}

    #if os(watchOS)
    private var supportedComplicationFamilies: [WidgetFamily] {
        [
            .accessoryCircular,
            .accessoryRectangular,
            .accessoryInline,
            .accessoryCorner
        ]
    }
    #else
    private var supportedComplicationFamilies: [WidgetFamily] {
        [
            .accessoryCircular,
            .accessoryRectangular,
            .accessoryInline
        ]
    }
    #endif

    public var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: Self.kind,
            provider: WatchDealTimelineProvider()
        ) { entry in
            HotDealsComplicationEntryView(entry: entry)
        }
        .configurationDisplayName("Course Deals Hot Coupons")
        .description("Glance at 100% OFF hot courses, ratings, and expiry timers on your watch face.")
        .supportedFamilies(supportedComplicationFamilies)
    }
}

/// Bundle combining Watch Face Complications and watchOS 10+ Smart Stack widget.
public struct WatchWidgetBundle: WidgetBundle {
    public init() {}

    public var body: some Widget {
        HotDealsComplicationWidget()
        SmartStackHotDealWidget()
    }
}
#endif
