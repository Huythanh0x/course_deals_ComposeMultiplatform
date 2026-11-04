import SwiftUI
import Combine
#if canImport(WatchKit)
import WatchKit
#endif

/// Main flash deals feed for Apple Watch companion app.
/// Presents top 5 expiring deals with Digital Crown scroll physics and real-time countdown tickers.
public struct WatchDealListView: View {
    @ObservedObject private var client = WatchConnectivityClient.shared
    @State private var selectedDealIndex: Int? = nil
    @State private var crownScrollPosition: CGFloat = 0.0
    @State private var now: Date = Date()

    private var deals: [WatchDealModel] {
        client.deals
    }

    // Refresh countdown every 30 seconds
    private let countdownTimer = Timer.publish(every: 30, on: .main, in: .common).autoconnect()

    public init() {}

    public var body: some View {
        NavigationStack {
            Group {
                if client.isLoading && deals.isEmpty {
                    loadingView
                } else if deals.isEmpty {
                    emptyView
                } else {
                    dealListContent
                }
            }
            .navigationTitle("Hot Deals")
            #if os(watchOS)
            .navigationBarTitleDisplayMode(.inline)
            #endif
            .sheet(item: selectedDealBinding) { _ in
                if let index = selectedDealIndex, client.deals.indices.contains(index) {
                    WatchDealDetailView(deal: $client.deals[index])
                }
            }
            .onOpenURL { url in
                handleWatchDeepLink(url)
            }
            .onAppear {
                client.requestDealsFromPhone()
                applyLaunchArgumentsIfNeeded()
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
                    applyLaunchArgumentsIfNeeded()
                }
            }
            .onReceive(countdownTimer) { currentTimestamp in
                now = currentTimestamp
            }
        }
    }

    private var dealListContent: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                // Flash summary pill & iPhone Sync Status
                HStack(spacing: 4) {
                    Image(systemName: "iphone.gen3")
                        .foregroundColor(.blue)
                        .font(.caption2)
                    Text(client.syncSource)
                        .font(.system(size: 9, weight: .semibold))
                        .foregroundColor(.cyan)
                        .lineLimit(1)
                        .accessibilityIdentifier("watch_sync_status_label")

                    Spacer()

                    Button(action: {
                        playHapticFeedback(.click)
                        client.requestDealsFromPhone()
                    }) {
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 10))
                            .foregroundColor(.secondary)
                    }
                    .buttonStyle(.plain)
                }
                .padding(.horizontal, 4)
                .padding(.top, 2)

                ForEach(Array(deals.prefix(5).enumerated()), id: \.element.id) { index, deal in
                    Button(action: {
                        playHapticFeedback(.click)
                        selectedDealIndex = index
                    }) {
                        WatchDealCard(deal: deal, now: now)
                    }
                    .buttonStyle(.plain)
                    .accessibilityIdentifier("watch_deal_card_\(deal.id)")
                }

                // Bottom refresh prompt
                Button(action: loadDeals) {
                    HStack {
                        Image(systemName: "arrow.clockwise")
                        Text("Refresh Feed")
                    }
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 2)
        }
        .accessibilityIdentifier("watch_deal_list")
        .focusable()
        #if os(watchOS)
        .digitalCrownRotation(
            $crownScrollPosition,
            from: 0,
            through: 100,
            by: 2.0,
            sensitivity: .medium,
            isContinuous: false,
            isHapticFeedbackEnabled: true
        )
        #endif
    }

    private var loadingView: some View {
        VStack(spacing: 12) {
            ProgressView()
                .progressViewStyle(.circular)
                .tint(.blue)
            Text("Finding Hot Deals...")
                .font(.footnote)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var emptyView: some View {
        VStack(spacing: 8) {
            Image(systemName: "flame.fill")
                .font(.title2)
                .foregroundColor(.orange)

            Text("No Hot Deals")
                .font(.headline)

            Text("No 4.7+ rated deals active right now. Check back shortly!")
                .font(.caption2)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 6)

            Button(action: loadDeals) {
                Text("Check Again")
                    .font(.caption2.bold())
            }
            .buttonStyle(.borderedProminent)
            .tint(.blue)
            .padding(.top, 4)
        }
        .padding(.horizontal, 6)
    }

    private var selectedDealBinding: Binding<WatchDealModel?> {
        Binding(
            get: {
                if let index = selectedDealIndex, deals.indices.contains(index) {
                    return deals[index]
                }
                return nil
            },
            set: { newValue in
                if newValue == nil {
                    selectedDealIndex = nil
                }
            }
        )
    }

    private func applyLaunchArgumentsIfNeeded() {
        let args = CommandLine.arguments
        if args.contains("--bookmark-top-deal"), let firstDeal = client.deals.first {
            if !firstDeal.isBookmarked {
                _ = client.toggleBookmark(for: firstDeal.id)
            }
        }
        if args.contains("--open-top-deal"), !client.deals.isEmpty {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) {
                selectedDealIndex = 0
            }
        }
    }

    private func handleWatchDeepLink(_ url: URL) {
        let raw = url.absoluteString.trimmingCharacters(in: .whitespacesAndNewlines)
        if raw.hasPrefix("coursedeals://feed") {
            selectedDealIndex = nil
            return
        }
        let prefixes = [
            "coursedeals://courses/",
            "coursedeals://coupon/",
            "https://coursedeals.com/deal/",
            "https://coursedeals.com/courses/"
        ]
        guard let prefix = prefixes.first(where: { raw.hasPrefix($0) }) else {
            return
        }
        let tail = String(raw.dropFirst(prefix.count))
            .components(separatedBy: CharacterSet(charactersIn: "?#/"))
            .first ?? ""
        guard let targetId = Int(tail), targetId > 0 else {
            return
        }
        if raw.contains("bookmark=1") {
            if !(client.deals.first(where: { $0.id == targetId })?.isBookmarked ?? false) {
                _ = client.toggleBookmark(for: targetId)
            }
        }
        if let index = client.deals.firstIndex(where: { $0.id == targetId }) {
            selectedDealIndex = index
        }
    }

    private func loadDeals() {
        client.requestDealsFromPhone()
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

/// Compact glanceable card displaying an expiring deal.
private struct WatchDealCard: View {
    let deal: WatchDealModel
    let now: Date

    var body: some View {
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

private enum WatchHapticType {
    case click
    case success
}
