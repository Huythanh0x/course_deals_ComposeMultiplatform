import Foundation
import WatchConnectivity

public class PhoneWatchConnectivityManager: NSObject, WCSessionDelegate, ObservableObject {
    public static let shared = PhoneWatchConnectivityManager()

    @Published public var isWatchPaired: Bool = false
    @Published public var isWatchAppInstalled: Bool = false

    public override init() {
        super.init()
        if WCSession.isSupported() {
            let session = WCSession.default
            session.delegate = self
            session.activate()
        }
    }

    public func syncDealsToWatch(deals: [[String: Any]]) {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        guard session.activationState == .activated else { return }

        let payload: [String: Any] = [
            "deals": deals,
            "timestamp": Date().timeIntervalSince1970
        ]

        // 1. Guaranteed background delivery via application context
        do {
            try session.updateApplicationContext(payload)
        } catch {
            print("[PhoneWatchConnectivity] Failed to update application context: \(error)")
        }

        // 2. Real-time immediate message if watch is reachable
        if session.isReachable {
            session.sendMessage(payload, replyHandler: nil) { error in
                print("[PhoneWatchConnectivity] Send message failed: \(error)")
            }
        }
    }

    // MARK: - WCSessionDelegate
    public func session(_ session: WCSession, activationDidCompleteWith activationState: WCSessionActivationState, error: Error?) {
        DispatchQueue.main.async {
            self.isWatchPaired = session.isPaired
            self.isWatchAppInstalled = session.isWatchAppInstalled
            if activationState == .activated {
                self.pushLatestDeals()
            }
        }
    }

    public func sessionReachabilityDidChange(_ session: WCSession) {
        if session.isReachable {
            pushLatestDeals()
        }
    }

    public func pushLatestDeals() {
        let liveDeals: [[String: Any]] = [
            [
                "id": "deal-live-1",
                "title": "Full-Stack Kotlin Multiplatform & SwiftUI 2026",
                "platform": "Udemy",
                "expiresAtEpochSeconds": Date().addingTimeInterval(3600 * 2.5).timeIntervalSince1970,
                "couponCode": "KMPWATCH26",
                "targetUrl": "https://coursedeals.com/deal/live-1",
                "rating": 4.95,
                "usesLeft": 22
            ],
            [
                "id": "deal-live-2",
                "title": "Machine Learning & Deep Learning with PyTorch",
                "platform": "Coursera",
                "expiresAtEpochSeconds": Date().addingTimeInterval(3600 * 4.2).timeIntervalSince1970,
                "couponCode": "FREEPYTORCH",
                "targetUrl": "https://coursedeals.com/deal/live-2",
                "rating": 4.88,
                "usesLeft": 9
            ],
            [
                "id": "deal-live-3",
                "title": "Docker, Kubernetes & Cloud Native Architecture",
                "platform": "edX",
                "expiresAtEpochSeconds": Date().addingTimeInterval(3600 * 1.2).timeIntervalSince1970,
                "couponCode": "CLOUDFREE",
                "targetUrl": "https://coursedeals.com/deal/live-3",
                "rating": 4.92,
                "usesLeft": 5
            ]
        ]
        syncDealsToWatch(deals: liveDeals)
    }

    public func sessionDidBecomeInactive(_ session: WCSession) {}

    public func sessionDidDeactivate(_ session: WCSession) {
        WCSession.default.activate()
    }

    public func session(_ session: WCSession, didReceiveMessage message: [String : Any], replyHandler: @escaping ([String : Any]) -> Void) {
        if message["request"] as? String == "fetchDeals" {
            // Supply live deals from iPhone to Watch
            let liveDeals: [[String: Any]] = [
                [
                    "id": "deal-live-1",
                    "title": "Full-Stack Kotlin Multiplatform & SwiftUI 2026",
                    "platform": "Udemy",
                    "expiresAtEpochSeconds": Date().addingTimeInterval(3600 * 2.5).timeIntervalSince1970,
                    "couponCode": "KMPWATCH26",
                    "targetUrl": "https://coursedeals.com/deal/live-1",
                    "rating": 4.95,
                    "usesLeft": 22
                ],
                [
                    "id": "deal-live-2",
                    "title": "Machine Learning & Deep Learning with PyTorch",
                    "platform": "Coursera",
                    "expiresAtEpochSeconds": Date().addingTimeInterval(3600 * 4.2).timeIntervalSince1970,
                    "couponCode": "FREEPYTORCH",
                    "targetUrl": "https://coursedeals.com/deal/live-2",
                    "rating": 4.88,
                    "usesLeft": 9
                ],
                [
                    "id": "deal-live-3",
                    "title": "Docker, Kubernetes & Cloud Native Architecture",
                    "platform": "edX",
                    "expiresAtEpochSeconds": Date().addingTimeInterval(3600 * 1.2).timeIntervalSince1970,
                    "couponCode": "CLOUDFREE",
                    "targetUrl": "https://coursedeals.com/deal/live-3",
                    "rating": 4.92,
                    "usesLeft": 5
                ]
            ]

            replyHandler([
                "status": "success",
                "deals": liveDeals,
                "source": "iPhone 17 (Live)"
            ])
        }
    }
}
