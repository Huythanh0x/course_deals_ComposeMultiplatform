import SwiftUI
#if canImport(WatchKit)
import WatchKit
#endif

/// Detail sheet for watchOS showing coupon code, Handoff to iPhone, and bookmark toggle.
public struct WatchDealDetailView: View {
    @Binding var deal: WatchDealModel
    @Environment(\.dismiss) private var dismiss

    @State private var hasCopiedCode: Bool = false
    @State private var hasTriggeredHandoff: Bool = false
    @State private var activeUserActivity: NSUserActivity?

    public static let handoffActivityType = "com.coursedeals.deal.open"

    public init(deal: Binding<WatchDealModel>) {
        self._deal = deal
    }

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 10) {
                // Header: Platform Tag & 100% OFF Badge
                HStack {
                    Text(deal.platform.displayName)
                        .font(.caption2.bold())
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(deal.platform.tagColor)
                        .foregroundColor(.white)
                        .cornerRadius(4)

                    Spacer()

                    Text(deal.discountBadgeText)
                        .font(.caption2.bold())
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.green)
                        .foregroundColor(.black)
                        .cornerRadius(4)
                }

                // Full Course Title
                Text(deal.title)
                    .font(.headline)
                    .foregroundColor(.primary)
                    .lineLimit(nil)
                    .fixedSize(horizontal: false, vertical: true)

                // Subtitle / Author
                if let author = deal.author {
                    Text("By \(author)")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }

                // Rating & Expiry
                HStack(spacing: 8) {
                    Text(deal.formattedRating)
                        .font(.caption2.bold())
                        .foregroundColor(.yellow)

                    if let remaining = deal.usesRemaining {
                        Text("• \(remaining) left")
                            .font(.caption2)
                            .foregroundColor(.orange)
                    }

                    Spacer()

                    HStack(spacing: 3) {
                        Image(systemName: "clock.fill")
                            .font(.system(size: 10))
                        Text(deal.formattedTimeLeft())
                            .font(.caption2.monospacedDigit())
                    }
                    .foregroundColor(.secondary)
                }

                Divider()
                    .padding(.vertical, 2)

                // Coupon Code Box with Haptic Feedback
                VStack(spacing: 4) {
                    Text("COUPON CODE")
                        .font(.system(size: 9, weight: .bold))
                        .foregroundColor(.secondary)

                    Button(action: copyCouponCode) {
                        HStack {
                            Image(systemName: hasCopiedCode ? "checkmark.circle.fill" : "doc.on.doc.fill")
                                .foregroundColor(hasCopiedCode ? .green : .accentColor)
                            Text(deal.couponCode ?? "FREE-COUPON")
                                .font(.system(size: 14, weight: .heavy, design: .monospaced))
                                .foregroundColor(.primary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                        .background(Color.white.opacity(0.12))
                        .cornerRadius(8)
                    }
                    .buttonStyle(.plain)

                    if hasCopiedCode {
                        Text("Code tapped! Ready to paste")
                            .font(.system(size: 10))
                            .foregroundColor(.green)
                    }
                }

                // Primary CTA: Open on iPhone (Apple Handoff)
                Button(action: triggerHandoffToIPhone) {
                    HStack {
                        Image(systemName: "iphone.and.arrow.forward")
                            .font(.headline)
                        Text(hasTriggeredHandoff ? "Sent to iPhone" : "Open on iPhone")
                            .font(.footnote.bold())
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                    .background(hasTriggeredHandoff ? Color.green : Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
                .buttonStyle(.plain)

                // Save for Later Bookmark Toggle
                Button(action: toggleBookmark) {
                    HStack {
                        Image(systemName: deal.isBookmarked ? "bookmark.fill" : "bookmark")
                            .foregroundColor(deal.isBookmarked ? .yellow : .primary)
                        Text(deal.isBookmarked ? "Saved for Later" : "Save for Later")
                            .font(.footnote)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 6)
                    .background(Color.gray.opacity(0.2))
                    .cornerRadius(8)
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 4)
        }
        .userActivity(Self.handoffActivityType, isActive: true) { activity in
            configureUserActivity(activity)
        }
        .onAppear {
            setupHandoffActivity()
        }
    }

    private func copyCouponCode() {
        playHapticFeedback(.click)
        hasCopiedCode = true

        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            hasCopiedCode = false
        }
    }

    private func toggleBookmark() {
        playHapticFeedback(.click)
        deal.isBookmarked.toggle()
    }

    private func triggerHandoffToIPhone() {
        playHapticFeedback(.success)
        hasTriggeredHandoff = true

        let activity = activeUserActivity ?? NSUserActivity(activityType: Self.handoffActivityType)
        configureUserActivity(activity)
        activity.becomeCurrent()
        activeUserActivity = activity

        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            hasTriggeredHandoff = false
        }
    }

    private func setupHandoffActivity() {
        let activity = NSUserActivity(activityType: Self.handoffActivityType)
        configureUserActivity(activity)
        activeUserActivity = activity
    }

    private func configureUserActivity(_ activity: NSUserActivity) {
        activity.title = deal.title
        activity.isEligibleForHandoff = true
        activity.isEligibleForSearch = true
        activity.isEligibleForPublicIndexing = false

        if let targetUrlString = deal.couponUrl, let targetUrl = URL(string: targetUrlString) {
            activity.webpageURL = targetUrl
        }

        var userInfo: [String: Any] = [
            "dealId": deal.id,
            "title": deal.title,
            "platform": deal.platform.displayName
        ]
        if let code = deal.couponCode {
            userInfo["couponCode"] = code
        }
        if let url = deal.couponUrl {
            userInfo["couponUrl"] = url
        }
        activity.userInfo = userInfo
    }

    private func playHapticFeedback(_ type: WatchHapticType) {
        #if canImport(WatchKit)
        let wkType: WKHapticType
        switch type {
        case .click: wkType = .click
        case .success: wkType = .success
        }
        WKInterfaceDevice.current().play(wkType)
        #endif
    }
}

private enum WatchHapticType {
    case click
    case success
}
