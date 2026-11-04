import Foundation
import SwiftUI
#if canImport(WidgetKit)
import WidgetKit
#endif

/// High-contrast watchOS 10+ Smart Stack card view displaying the top trending hot deal.
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
        VStack(alignment: .leading, spacing: 3) {
            // Top Smart Stack header row: 🔥 HOT, bookmark indicator, and countdown timer
            HStack(spacing: 4) {
                Text("🔥 HOT")
                    .font(.system(size: 8, weight: .heavy))
                    .lineLimit(1)
                    .fixedSize(horizontal: true, vertical: false)
                    .padding(.horizontal, 4)
                    .padding(.vertical, 1.5)
                    .background(Color.red)
                    .foregroundColor(.white)
                    .cornerRadius(3)

                if bookmarkedCount > 0 || (deal?.isBookmarked == true) {
                    HStack(spacing: 2) {
                        Image(systemName: "bookmark.fill")
                            .font(.system(size: 8))
                        if bookmarkedCount > 0 {
                            Text("\(bookmarkedCount)")
                                .font(.system(size: 8, weight: .bold))
                        }
                    }
                    .foregroundColor(.yellow)
                }

                Spacer(minLength: 2)

                if let deal = deal {
                    HStack(spacing: 2) {
                        Image(systemName: "clock.fill")
                            .font(.system(size: 8))
                        Text(deal.formattedTimeLeft(now: now))
                            .font(.system(size: 8.5, weight: .semibold, design: .monospaced))
                    }
                    .foregroundColor(.orange)
                }
            }

            if let deal = deal {
                Text(deal.title)
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(.white)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)

                HStack(spacing: 4) {
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

                    Text("Code: \(deal.couponCode ?? "HOT100")")
                        .font(.system(size: 8, weight: .bold, design: .monospaced))
                        .foregroundColor(.cyan)
                        .lineLimit(1)
                }
            } else {
                Text("No active hot deals in offline cache")
                    .font(.system(size: 10, weight: .medium))
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 2)
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
            if #available(watchOS 10.0, iOS 17.0, *) {
                SmartStackHotDealCardView(
                    deal: entry.topDeal,
                    activeDealCount: entry.activeDealCount,
                    bookmarkedCount: entry.bookmarkedCount,
                    now: entry.date
                )
                .widgetURL(entry.topDeal?.deepLinkUrl)
                .containerBackground(for: .widget) {
                    LinearGradient(
                        colors: [
                            Color(red: 0.10, green: 0.13, blue: 0.22),
                            Color(red: 0.05, green: 0.07, blue: 0.12)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                }
            } else {
                SmartStackHotDealCardView(
                    deal: entry.topDeal,
                    activeDealCount: entry.activeDealCount,
                    bookmarkedCount: entry.bookmarkedCount,
                    now: entry.date
                )
                .widgetURL(entry.topDeal?.deepLinkUrl)
            }
        }
        .configurationDisplayName("Smart Stack Hot Deal")
        .description("watchOS 10+ Smart Stack card highlighting the highest-rated hot course.")
        .supportedFamilies([.accessoryRectangular])
    }
}
#endif
