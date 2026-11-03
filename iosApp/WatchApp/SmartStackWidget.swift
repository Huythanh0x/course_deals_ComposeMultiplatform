import Foundation
import SwiftUI
#if canImport(WidgetKit)
import WidgetKit
#endif

/// High-contrast watchOS 10+ Smart Stack card view displaying the top trending 100% OFF deal.
public struct SmartStackHotDealCardView: View {
    public let deal: WatchDealModel?
    public let activeDealCount: Int
    public let bookmarkedCount: Int
    public let now: Date

    public init(
        deal: WatchDealModel?,
        activeDealCount: Int,
        bookmarkedCount: Int = 0,
        now: Date = Date()
    ) {
        self.deal = deal
        self.activeDealCount = activeDealCount
        self.bookmarkedCount = bookmarkedCount
        self.now = now
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            // Top Smart Stack header row
            HStack(spacing: 4) {
                HStack(spacing: 2) {
                    Image(systemName: "square.stack.3d.up.fill")
                        .font(.system(size: 8, weight: .bold))
                    Text("SMART STACK")
                        .font(.system(size: 8, weight: .heavy))
                }
                .padding(.horizontal, 5)
                .padding(.vertical, 2)
                .background(Color.blue.opacity(0.35))
                .foregroundColor(.cyan)
                .cornerRadius(4)

                if let deal = deal {
                    Text(deal.platform.displayName)
                        .font(.system(size: 8, weight: .bold))
                        .padding(.horizontal, 5)
                        .padding(.vertical, 2)
                        .background(deal.platform.tagColor)
                        .foregroundColor(.white)
                        .cornerRadius(4)
                }

                Spacer()

                Text(deal?.discountBadgeText ?? "100% OFF")
                    .font(.system(size: 8, weight: .heavy))
                    .padding(.horizontal, 5)
                    .padding(.vertical, 2)
                    .background(Color.green)
                    .foregroundColor(.black)
                    .cornerRadius(4)
            }

            if let deal = deal {
                Text(deal.title)
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(.white)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)

                HStack(spacing: 5) {
                    Text(deal.formattedRating)
                        .font(.system(size: 9, weight: .bold))
                        .foregroundColor(.yellow)

                    if !deal.formattedStudents.isEmpty {
                        Text("• \(deal.formattedStudents)")
                            .font(.system(size: 8))
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }

                    Spacer()

                    HStack(spacing: 2) {
                        Image(systemName: "clock.fill")
                            .font(.system(size: 8))
                        Text(deal.formattedTimeLeft(now: now))
                            .font(.system(size: 9, weight: .semibold, design: .monospaced))
                    }
                    .foregroundColor(.orange)
                }

                HStack(spacing: 4) {
                    Image(systemName: "tag.fill")
                        .font(.system(size: 8))
                        .foregroundColor(.green)
                    Text("Code: \(deal.couponCode ?? "HOT100")")
                        .font(.system(size: 8, weight: .bold, design: .monospaced))
                        .foregroundColor(.green)
                    Spacer()
                    if bookmarkedCount > 0 {
                        HStack(spacing: 2) {
                            Image(systemName: "bookmark.fill")
                                .font(.system(size: 8))
                            Text("\(bookmarkedCount)")
                                .font(.system(size: 8, weight: .bold))
                        }
                        .foregroundColor(.yellow)
                    }
                }
            } else {
                Text("No active hot deals in offline cache")
                    .font(.system(size: 10, weight: .medium))
                    .foregroundColor(.secondary)
            }
        }
        .padding(8)
        .background(
            LinearGradient(
                colors: [
                    Color(red: 0.10, green: 0.13, blue: 0.22),
                    Color(red: 0.05, green: 0.07, blue: 0.12)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.cyan.opacity(0.35), lineWidth: 1)
        )
    }
}

#if canImport(WidgetKit) && (os(watchOS) || os(iOS))
/// Dedicated watchOS 10+ Smart Stack Widget displaying the top trending deal card.
public struct SmartStackHotDealWidget: Widget {
    public static let kind = "com.thanh0x.coursedeals.watch.smartstack"

    public init() {}

    public var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: Self.kind,
            provider: WatchDealTimelineProvider()
        ) { entry in
            SmartStackHotDealCardView(
                deal: entry.topDeal,
                activeDealCount: entry.activeDealCount,
                bookmarkedCount: entry.bookmarkedCount,
                now: entry.date
            )
            .widgetURL(entry.topDeal?.deepLinkUrl)
        }
        .configurationDisplayName("Smart Stack Hot Deal")
        .description("watchOS 10+ Smart Stack card highlighting the highest-rated 100% OFF deal.")
        .supportedFamilies([.accessoryRectangular])
    }
}
#endif

/// Interactive in-app gallery sheet rendering live WatchOS 10+ Smart Stack and Watch Face Complications
/// populated from `WatchDealCacheStore` so users and QA can inspect and tap complications directly.
public struct WatchComplicationsPreviewSheet: View {
    @ObservedObject private var client = WatchConnectivityClient.shared
    @Environment(\.dismiss) private var dismiss

    public var onSelectDeal: ((WatchDealModel) -> Void)?

    public init(onSelectDeal: ((WatchDealModel) -> Void)? = nil) {
        self.onSelectDeal = onSelectDeal
    }

    private var currentEntry: WatchDealTimelineEntry {
        let cachedEntry = WatchDealTimelineProvider(cacheStore: .shared).buildCurrentEntry()
        if cachedEntry.topDeal != nil {
            return cachedEntry
        }
        if let firstLive = client.deals.first {
            return WatchDealTimelineEntry(
                date: Date(),
                topDeal: firstLive,
                activeDealCount: client.deals.count,
                bookmarkedCount: client.deals.filter { $0.isBookmarked }.count
            )
        }
        return WatchDealTimelineEntry.sample()
    }

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 10) {
                // Header
                HStack {
                    Image(systemName: "applewatch.watchface")
                        .foregroundColor(.cyan)
                        .font(.caption)
                    Text("Watch Widgets")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.white)
                    Spacer()
                    Text("\(currentEntry.activeDealCount) Active")
                        .font(.system(size: 9, weight: .semibold))
                        .padding(.horizontal, 5)
                        .padding(.vertical, 2)
                        .background(Color.orange.opacity(0.25))
                        .foregroundColor(.orange)
                        .cornerRadius(4)
                }

                // 1. watchOS 10+ Smart Stack Card
                Text("WATCHOS 10+ SMART STACK")
                    .font(.system(size: 8, weight: .heavy))
                    .foregroundColor(.secondary)

                Button(action: {
                    if let topDeal = currentEntry.topDeal {
                        dismiss()
                        onSelectDeal?(topDeal)
                    }
                }) {
                    SmartStackHotDealCardView(
                        deal: currentEntry.topDeal,
                        activeDealCount: currentEntry.activeDealCount,
                        bookmarkedCount: currentEntry.bookmarkedCount,
                        now: currentEntry.date
                    )
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("watch_smart_stack_card")

                // 2. Watch Face Complications (.accessoryCircular + .accessoryCorner)
                Text("WATCH FACE COMPLICATIONS")
                    .font(.system(size: 8, weight: .heavy))
                    .foregroundColor(.secondary)

                HStack(spacing: 10) {
                    VStack(spacing: 3) {
                        CircularHotDealsComplicationView(activeDealCount: currentEntry.activeDealCount)
                        Text("Circular")
                            .font(.system(size: 8))
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 6) {
                        CornerHotDealsComplicationView(activeDealCount: currentEntry.activeDealCount)
                            .padding(6)
                            .background(Color.white.opacity(0.08))
                            .cornerRadius(8)
                        Text("Corner + Gauge")
                            .font(.system(size: 8))
                            .foregroundColor(.secondary)
                    }
                }
                .padding(.horizontal, 4)

                // 3. Rectangular Complication (.accessoryRectangular)
                Button(action: {
                    if let topDeal = currentEntry.topDeal {
                        dismiss()
                        onSelectDeal?(topDeal)
                    }
                }) {
                    RectangularHotDealComplicationView(
                        deal: currentEntry.topDeal,
                        activeDealCount: currentEntry.activeDealCount,
                        now: currentEntry.date
                    )
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("watch_rectangular_complication")

                // 4. Inline Complication (.accessoryInline)
                VStack(alignment: .leading, spacing: 2) {
                    Text("INLINE COMPLICATION")
                        .font(.system(size: 8, weight: .heavy))
                        .foregroundColor(.secondary)
                    InlineHotDealComplicationView(
                        deal: currentEntry.topDeal,
                        activeDealCount: currentEntry.activeDealCount,
                        now: currentEntry.date
                    )
                    .padding(5)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.white.opacity(0.06))
                    .cornerRadius(6)
                }
            }
            .padding(.horizontal, 4)
            .padding(.vertical, 4)
        }
        .accessibilityIdentifier("watch_complication_preview_sheet")
    }
}
