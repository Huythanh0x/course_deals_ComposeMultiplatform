import SwiftUI
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
            .navigationTitle("Flash Deals")
            .navigationBarTitleDisplayMode(.inline)
            .sheet(item: selectedDealBinding) { _ in
                if let index = selectedDealIndex, client.deals.indices.contains(index) {
                    WatchDealDetailView(deal: $client.deals[index])
                }
            }
            .onAppear {
                client.requestDealsFromPhone()
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
                HStack {
                    Image(systemName: "iphone.gen3")
                        .foregroundColor(.blue)
                        .font(.caption2)
                    Text(client.syncSource)
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundColor(.cyan)
                        .lineLimit(1)
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
        .focusable()
        .digitalCrownRotation(
            $crownScrollPosition,
            from: 0,
            through: 100,
            by: 2.0,
            sensitivity: .medium,
            isContinuous: false,
            isHapticFeedbackEnabled: true
        )
    }

    private var loadingView: some View {
        VStack(spacing: 12) {
            ProgressView()
                .progressViewStyle(.circular)
                .tint(.blue)
            Text("Finding Free Deals...")
                .font(.footnote)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var emptyView: some View {
        VStack(spacing: 8) {
            Image(systemName: "ticket")
                .font(.title2)
                .foregroundColor(.secondary)

            Text("No Flash Deals")
                .font(.headline)

            Text("All 100% coupons are currently claimed. Check back shortly!")
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
            // Badges row
            HStack(spacing: 4) {
                Text(deal.platform.displayName)
                    .font(.system(size: 9, weight: .bold))
                    .padding(.horizontal, 5)
                    .padding(.vertical, 1.5)
                    .background(deal.platform.tagColor)
                    .foregroundColor(.white)
                    .cornerRadius(3)

                Text(deal.discountBadgeText)
                    .font(.system(size: 9, weight: .heavy))
                    .padding(.horizontal, 4)
                    .padding(.vertical, 1.5)
                    .background(Color.green)
                    .foregroundColor(.black)
                    .cornerRadius(3)

                Spacer()

                if deal.isBookmarked {
                    Image(systemName: "bookmark.fill")
                        .font(.system(size: 9))
                        .foregroundColor(.yellow)
                }

                HStack(spacing: 2) {
                    Image(systemName: "clock")
                        .font(.system(size: 8))
                    Text(deal.formattedTimeLeft(now: now))
                        .font(.system(size: 9, weight: .semibold, design: .monospaced))
                }
                .foregroundColor(.orange)
            }

            // Title
            Text(deal.title)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(.primary)
                .lineLimit(2)
                .multilineTextAlignment(.leading)

            // Rating & Remaining
            HStack(spacing: 6) {
                Text(deal.formattedRating)
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.yellow)

                if let remaining = deal.usesRemaining {
                    Text("\(remaining) uses left")
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
