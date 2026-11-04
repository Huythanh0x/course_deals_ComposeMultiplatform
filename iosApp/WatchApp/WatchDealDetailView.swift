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

    public static let handoffActivityType = "com.thanh0x.coursedeals.courseDetail"
    public static let legacyHandoffActivityType = "com.coursedeals.deal.open"

    public init(deal: Binding<WatchDealModel>) {
        self._deal = deal
    }

    public static func buildHandoffUserInfo(for deal: WatchDealModel) -> [String: Any] {
        var userInfo: [String: Any] = [
            "courseId": deal.id,
            "dealId": deal.id,
            "deepLink": deal.deepLinkUrl.absoluteString,
            "title": deal.title,
            "platform": deal.platform.displayName
        ]
        if let code = deal.couponCode {
            userInfo["couponCode"] = code
        }
        if let url = deal.couponUrl {
            userInfo["couponUrl"] = url
        }
        return userInfo
    }

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 5) {
                // Header: 🔥 HOT Badge, Bookmark Indicator & Countdown Timer
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
                        Image(systemName: "clock.fill")
                            .font(.system(size: 8.5))
                        Text(deal.formattedTimeLeft())
                            .font(.system(size: 9, weight: .semibold, design: .monospaced))
                            .lineLimit(1)
                            .fixedSize(horizontal: true, vertical: false)
                    }
                    .foregroundColor(.orange)
                }

                // Full Course Title
                Text(deal.title)
                    .font(.system(size: 11.5, weight: .bold))
                    .foregroundColor(.primary)
                    .lineLimit(2)
                    .fixedSize(horizontal: false, vertical: true)

                // Rating & Students
                HStack(spacing: 4) {
                    Text(deal.formattedRating)
                        .font(.system(size: 9.5, weight: .bold))
                        .foregroundColor(.yellow)

                    if !deal.formattedStudents.isEmpty {
                        Text("• \(deal.formattedStudents)")
                            .font(.system(size: 9))
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }

                    Spacer(minLength: 2)
                }

                // Coupon Code Box with Haptic Feedback
                Button(action: copyCouponCode) {
                    HStack(spacing: 5) {
                        Image(systemName: hasCopiedCode ? "checkmark.circle.fill" : "doc.on.doc.fill")
                            .font(.system(size: 10))
                            .foregroundColor(hasCopiedCode ? .green : .cyan)
                        Text("CODE: \(deal.couponCode ?? "FREE-COUPON")")
                            .font(.system(size: 10.5, weight: .heavy, design: .monospaced))
                            .foregroundColor(.primary)
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 5)
                    .background(Color.white.opacity(0.12))
                    .cornerRadius(6)
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("watch_copy_coupon_button")

                // Primary CTA: Open on iPhone (Apple Handoff + WCSession instant relay)
                Button(action: { triggerHandoffToIPhone() }) {
                    HStack(spacing: 5) {
                        Image(systemName: "iphone.and.arrow.forward")
                            .font(.system(size: 11, weight: .bold))
                        Text(hasTriggeredHandoff ? "Sent to iPhone" : "Open on iPhone")
                            .font(.system(size: 11, weight: .bold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 6)
                    .background(hasTriggeredHandoff ? Color.green : Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(7)
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("watch_open_on_iphone_button")

                // Save for Later Bookmark Toggle (two-way synced with iPhone & offline cache)
                Button(action: toggleBookmark) {
                    HStack(spacing: 5) {
                        Image(systemName: deal.isBookmarked ? "bookmark.fill" : "bookmark")
                            .font(.system(size: 10.5))
                            .foregroundColor(deal.isBookmarked ? .yellow : .primary)
                        Text(deal.isBookmarked ? "Saved for Later" : "Save for Later")
                            .font(.system(size: 10.5, weight: .semibold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 5)
                    .background(Color.gray.opacity(0.24))
                    .cornerRadius(7)
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("watch_bookmark_button")
            }
            .padding(.horizontal, 4)
        }
        .userActivity(Self.handoffActivityType, isActive: true) { activity in
            configureUserActivity(activity)
        }
        .onAppear {
            setupHandoffActivity()
            if CommandLine.arguments.contains("--trigger-handoff") {
                if !deal.isBookmarked {
                    let newState = WatchConnectivityClient.shared.toggleBookmark(for: deal.id)
                    deal.isBookmarked = newState
                }
                triggerHandoffToIPhone(resetDelay: 12.0)
            }
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
        let newState = WatchConnectivityClient.shared.toggleBookmark(for: deal.id)
        deal.isBookmarked = newState
    }

    private func triggerHandoffToIPhone(resetDelay: TimeInterval = 2.5) {
        playHapticFeedback(.success)
        hasTriggeredHandoff = true

        let activity = activeUserActivity ?? NSUserActivity(activityType: Self.handoffActivityType)
        configureUserActivity(activity)
        activity.becomeCurrent()
        activeUserActivity = activity

        // Simultaneously relay via WCSession + shared event file so paired iPhone navigates immediately
        WatchConnectivityClient.shared.sendOpenOnPhoneRequest(for: deal)

        DispatchQueue.main.asyncAfter(deadline: .now() + resetDelay) {
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
        } else {
            activity.webpageURL = URL(string: "https://coursedeals.com/deal/\(deal.id)")
        }

        activity.userInfo = Self.buildHandoffUserInfo(for: deal)
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
