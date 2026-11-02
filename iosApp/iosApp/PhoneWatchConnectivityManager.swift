import Foundation
import WatchConnectivity

public class PhoneWatchConnectivityManager: NSObject, WCSessionDelegate, ObservableObject {
    public static let shared = PhoneWatchConnectivityManager()

    @Published public var isWatchPaired: Bool = false
    @Published public var isWatchAppInstalled: Bool = false

    private let apiEndpoint = URL(string: "http://127.0.0.1:8081/api/v1/coupons?page=0&size=20")!

    public override init() {
        super.init()
        if WCSession.isSupported() {
            let session = WCSession.default
            session.delegate = self
            session.activate()
        }
    }

    public func fetchLiveDealsFromAPI(completion: @escaping ([[String: Any]]) -> Void) {
        var request = URLRequest(url: apiEndpoint)
        request.httpMethod = "GET"
        request.timeoutInterval = 5.0
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                print("[PhoneWatchConnectivity] Live API error: \(String(describing: error))")
                completion([])
                return
            }

            do {
                if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let courses = json["courses"] as? [[String: Any]] {
                    let mappedDeals: [[String: Any]] = courses.prefix(5).map { course in
                        let idVal = "\(course["courseId"] ?? 0)"
                        let title = course["title"] as? String ?? "Course Deal"
                        let author = course["author"] as? String ?? "Instructor"
                        let expiredTimeNum = (course["expiredTime"] as? NSNumber)?.doubleValue ?? Date().addingTimeInterval(7200).timeIntervalSince1970
                        let code = course["couponCode"] as? String ?? "FREE100"
                        let previewImage = course["previewImage"] as? String ?? ""
                        let rating = (course["rating"] as? NSNumber)?.doubleValue ?? 4.8
                        let reviews = (course["reviews"] as? NSNumber)?.intValue ?? 10

                        let platform: String
                        if previewImage.contains("udemy") {
                            platform = "Udemy"
                        } else if previewImage.contains("coursera") {
                            platform = "Coursera"
                        } else {
                            platform = "edX"
                        }

                        return [
                            "id": idVal,
                            "title": title,
                            "author": author,
                            "platform": platform,
                            "expiresAtEpochSeconds": expiredTimeNum,
                            "couponCode": code,
                            "targetUrl": "https://coursedeals.com/deal/\(idVal)",
                            "rating": rating,
                            "usesLeft": max(5, reviews / 10)
                        ]
                    }
                    completion(mappedDeals)
                    return
                }
            } catch {
                print("[PhoneWatchConnectivity] JSON parsing error: \(error)")
            }
            completion([])
        }
        task.resume()
    }

    public func syncDealsToWatch(deals: [[String: Any]]) {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        guard session.activationState == .activated else { return }

        let payload: [String: Any] = [
            "deals": deals,
            "timestamp": Date().timeIntervalSince1970,
            "source": "Spring Boot :8081 via iPhone"
        ]

        do {
            try session.updateApplicationContext(payload)
        } catch {
            print("[PhoneWatchConnectivity] Failed to update application context: \(error)")
        }

        if session.isReachable {
            session.sendMessage(payload, replyHandler: nil) { error in
                print("[PhoneWatchConnectivity] Send message failed: \(error)")
            }
        }
    }

    public func pushLatestDeals() {
        fetchLiveDealsFromAPI { [weak self] liveDeals in
            if !liveDeals.isEmpty {
                self?.syncDealsToWatch(deals: liveDeals)
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

    public func sessionDidBecomeInactive(_ session: WCSession) {}

    public func sessionDidDeactivate(_ session: WCSession) {
        WCSession.default.activate()
    }

    public func session(_ session: WCSession, didReceiveMessage message: [String : Any], replyHandler: @escaping ([String : Any]) -> Void) {
        if message["request"] as? String == "fetchDeals" {
            fetchLiveDealsFromAPI { liveDeals in
                if !liveDeals.isEmpty {
                    replyHandler([
                        "status": "success",
                        "deals": liveDeals,
                        "source": "Spring Boot DB via iPhone"
                    ])
                } else {
                    replyHandler([
                        "status": "error",
                        "deals": [],
                        "source": "API Unreachable"
                    ])
                }
            }
        }
    }
}
