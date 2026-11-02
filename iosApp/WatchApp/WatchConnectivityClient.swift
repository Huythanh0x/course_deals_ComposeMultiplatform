import Foundation
import SwiftUI
import WatchConnectivity

public class WatchConnectivityClient: NSObject, WCSessionDelegate, ObservableObject {
    public static let shared = WatchConnectivityClient()

    @Published public var deals: [WatchDealModel] = []
    @Published public var isConnectedToPhone: Bool = false
    @Published public var syncSource: String = "Syncing with iPhone 17..."
    @Published public var isLoading: Bool = false

    public override init() {
        super.init()
        if WCSession.isSupported() {
            let session = WCSession.default
            session.delegate = self
            session.activate()
        }
        // Load initial fallback while connecting
        self.deals = WatchDealModel.sampleDeals
        requestDealsFromPhone()
    }

    public func requestDealsFromPhone() {
        guard WCSession.isSupported() else {
            self.syncSource = "Standalone Mode"
            return
        }

        let session = WCSession.default
        guard session.activationState == .activated else {
            self.syncSource = "Activating WCSession..."
            session.activate()
            return
        }

        self.isLoading = true
        self.syncSource = "Requesting from iPhone 17..."

        if session.isReachable {
            session.sendMessage(["request": "fetchDeals"]) { [weak self] reply in
                DispatchQueue.main.async {
                    self?.handlePhoneReply(reply)
                }
            } errorHandler: { [weak self] error in
                DispatchQueue.main.async {
                    self?.isLoading = false
                    self?.syncSource = "iPhone unreachable (\(error.localizedDescription))"
                }
            }
        } else {
            // Check application context if already received
            let context = session.receivedApplicationContext
            if let dealsData = context["deals"] as? [[String: Any]], !dealsData.isEmpty {
                self.deals = parseDeals(from: dealsData)
                self.syncSource = "Synced via iPhone Context"
                self.isLoading = false
            } else {
                self.syncSource = "iPhone 17 (Pair Connected)"
                self.isLoading = false
            }
        }
    }

    private func handlePhoneReply(_ reply: [String: Any]) {
        self.isLoading = false
        if let dealsData = reply["deals"] as? [[String: Any]] {
            self.deals = parseDeals(from: dealsData)
            let source = reply["source"] as? String ?? "iPhone 17"
            self.syncSource = "⚡ Live from \(source)"
        }
    }

    private func parseDeals(from data: [[String: Any]]) -> [WatchDealModel] {
        return data.compactMap { (dict: [String: Any]) -> WatchDealModel? in
            guard let title = dict["title"] as? String,
                  let platformStr = dict["platform"] as? String,
                  let expiresAt = dict["expiresAtEpochSeconds"] as? Double else {
                return nil
            }
            let idInt = (dict["id"] as? Int) ?? (dict["usesLeft"] as? Int) ?? 100

            let platform: WatchCoursePlatform
            switch platformStr.lowercased() {
            case "udemy": platform = .udemy
            case "coursera": platform = .coursera
            case "edx": platform = .edx
            default: platform = .udemy
            }

            return WatchDealModel(
                id: idInt > 0 ? idInt : Int.random(in: 100...999),
                title: title,
                heading: nil,
                author: "Course Deals Live",
                category: "Programming",
                couponCode: dict["couponCode"] as? String ?? "FREEDEAL",
                couponUrl: dict["targetUrl"] as? String ?? "https://coursedeals.com",
                expiredTime: Int64(expiresAt),
                rating: dict["rating"] as? Double ?? 4.9,
                reviews: 120,
                usesRemaining: dict["usesLeft"] as? Int ?? 10,
                platform: platform,
                isBookmarked: false
            )
        }
    }

    // MARK: - WCSessionDelegate
    public func session(_ session: WCSession, activationDidCompleteWith activationState: WCSessionActivationState, error: Error?) {
        DispatchQueue.main.async {
            self.isConnectedToPhone = session.isReachable
            if activationState == .activated {
                self.requestDealsFromPhone()
            }
        }
    }

    public func sessionReachabilityDidChange(_ session: WCSession) {
        DispatchQueue.main.async {
            self.isConnectedToPhone = session.isReachable
            if session.isReachable {
                self.requestDealsFromPhone()
            }
        }
    }

    public func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String : Any]) {
        DispatchQueue.main.async {
            if let dealsData = applicationContext["deals"] as? [[String: Any]] {
                self.deals = self.parseDeals(from: dealsData)
                self.syncSource = "⚡ Synced from iPhone 17"
            }
        }
    }

    public func session(_ session: WCSession, didReceiveMessage message: [String : Any]) {
        DispatchQueue.main.async {
            if let dealsData = message["deals"] as? [[String: Any]] {
                self.deals = self.parseDeals(from: dealsData)
                self.syncSource = "⚡ Live push from iPhone 17"
            }
        }
    }
}
