import Foundation
import SwiftUI
#if canImport(WidgetKit)
import WidgetKit
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

/// `.accessoryRectangular` complication showing top hot deal title, rating, and countdown.
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
                    Text("🔥 HOT")
                        .font(.system(size: 8.5, weight: .heavy))
                        .foregroundColor(.orange)
                    if deal.isBookmarked {
                        Image(systemName: "bookmark.fill")
                            .font(.system(size: 8))
                            .foregroundColor(.yellow)
                    }
                    Spacer()
                    Text(deal.formattedTimeLeft(now: now))
                        .font(.system(size: 8.5, weight: .semibold, design: .monospaced))
                        .foregroundColor(.cyan)
                }

                Text(deal.title)
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(.white)
                    .lineLimit(1)

                HStack(spacing: 5) {
                    Text(deal.formattedRating)
                        .font(.system(size: 9, weight: .bold))
                        .foregroundColor(.yellow)
                    if !deal.formattedStudents.isEmpty {
                        Text("• \(deal.formattedStudents)")
                            .font(.system(size: 8.5))
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
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
                Text("Tap to refresh hot coupons")
                    .font(.system(size: 8))
                    .foregroundColor(.cyan)
            }
        }
        .padding(4)
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
            return "🔥 \(max(1, activeDealCount)) Hot Deals • \(deal.formattedRating) • \(deal.formattedTimeLeft(now: now))"
        }
        return "🔥 Course Deals • Hot Coupons"
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
            Text("\(activeDealCount) HOT")
                .font(.system(size: 10, weight: .heavy, design: .rounded))
                .foregroundColor(.orange)
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

    @ViewBuilder
    private var complicationContent: some View {
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

    public var body: some View {
        if #available(watchOS 10.0, iOS 17.0, *) {
            complicationContent
                .widgetURL(entry.topDeal?.deepLinkUrl)
                .containerBackground(for: .widget) {
                    Color.clear
                }
        } else {
            complicationContent
                .widgetURL(entry.topDeal?.deepLinkUrl)
        }
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
        .description("Glance at hot courses, ratings, and expiry timers on your watch face.")
        .supportedFamilies(supportedComplicationFamilies)
    }
}
#endif
