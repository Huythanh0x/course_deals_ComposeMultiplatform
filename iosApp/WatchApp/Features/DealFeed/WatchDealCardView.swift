import SwiftUI

/// Compact glanceable card displaying an expiring deal in the Apple Watch feed.
public struct WatchDealCardView: View {
    public let deal: WatchDealModel
    public let now: Date

    public init(deal: WatchDealModel, now: Date = Date()) {
        self.deal = deal
        self.now = now
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            // Badges row: only 🔥 HOT, bookmark indicator, and countdown timer
            HStack(spacing: 4) {
                if deal.isHot {
                    Text("🔥 HOT")
                        .font(.system(size: 8.5, weight: .heavy))
                        .lineLimit(1)
                        .fixedSize(horizontal: true, vertical: false)
                        .padding(.horizontal, 4)
                        .padding(.vertical, 1.5)
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(3)
                }

                Spacer(minLength: 2)

                if deal.isBookmarked {
                    Image(systemName: "bookmark.fill")
                        .font(.system(size: 8.5))
                        .foregroundColor(.yellow)
                }

                HStack(spacing: 2) {
                    Image(systemName: "clock")
                        .font(.system(size: 8))
                    Text(deal.formattedTimeLeft(now: now))
                        .font(.system(size: 8.5, weight: .semibold, design: .monospaced))
                        .lineLimit(1)
                        .fixedSize(horizontal: true, vertical: false)
                }
                .foregroundColor(.orange)
            }

            // Title
            Text(deal.title)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(.primary)
                .lineLimit(2)
                .multilineTextAlignment(.leading)

            // Rating & Students & Remaining
            HStack(spacing: 5) {
                Text(deal.formattedRating)
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.yellow)

                if !deal.formattedStudents.isEmpty {
                    Text("• \(deal.formattedStudents)")
                        .font(.system(size: 9, weight: .medium))
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }

                if let remaining = deal.usesRemaining {
                    Text("• \(remaining) left")
                        .font(.system(size: 9))
                        .foregroundColor(.secondary)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 8, weight: .bold))
                    .foregroundColor(.secondary.opacity(0.6))
            }
        }
        .padding(8)
        .background(Color.white.opacity(0.08))
        .cornerRadius(10)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(Color.white.opacity(0.12), lineWidth: 0.8)
        )
    }
}
