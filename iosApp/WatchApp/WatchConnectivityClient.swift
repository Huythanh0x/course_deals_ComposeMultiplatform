import Foundation
import SwiftUI
import WatchConnectivity

public class WatchConnectivityClient: NSObject, WCSessionDelegate, ObservableObject {
    public static let shared = WatchConnectivityClient()

    @Published public var deals: [WatchDealModel] = []
    @Published public var isConnectedToPhone: Bool = false
    @Published public var syncSource: String = "Connecting..."
    @Published public var isLoading: Bool = false

    private let directApiEndpoint = URL(string: "http://127.0.0.1:8081/api/v1/coupons?page=0&size=5")!

    public override init() {
        super.init()
        if WCSession.isSupported() {
            let session = WCSession.default
            session.delegate = self
            session.activate()
        }
        self.deals = WatchDealModel.sampleDeals
        requestDealsFromPhone()
    }

    public func requestDealsFromPhone() {
        self.isLoading = true
        self.syncSource = "Fetching from API..."

        let session = WCSession.default
        if WCSession.isSupported() && session.activationState == .activated && session.isReachable {
            session.sendMessage(["request": "fetchDeals"]) { [weak self] reply in
                DispatchQueue.main.async {
                    self?.handlePhoneReply(reply)
                }
            } errorHandler: { [weak self] _ in
                DispatchQueue.main.async {
                    // Fallback to direct network fetch
                    self?.fetchDirectlyFromApi()
                }
            }
        } else {
            // Check application context or fetch directly from API
            if let contextDeals = session.receivedApplicationContext["deals"] as? [[String: Any]], !contextDeals.isEmpty {
                self.deals = self.parseDeals(from: contextDeals)
                self.syncSource = "⚡ Synced via iPhone Context"
                self.isLoading = false
            } else {
                fetchDirectlyFromApi()
            }
        }
    }

    public func fetchDirectlyFromApi() {
        var request = URLRequest(url: directApiEndpoint)
        request.httpMethod = "GET"
        request.timeoutInterval = 4.0
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let task = URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                guard let data = data, error == nil else {
                    self?.syncSource = "Offline (API Unreachable)"
                    return
                }

                do {
                    if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
                       let courses = json["courses"] as? [[String: Any]], !courses.isEmpty {
                        self?.deals = self?.parseApiCourses(from: courses) ?? []
                        self?.syncSource = "🌐 Live from API :8081"
                        return
                    }
                } catch {
                    self?.syncSource = "API Parse Error"
                }
            }
        }
        task.resume()
    }

    private func handlePhoneReply(_ reply: [String: Any]) {
        self.isLoading = false
        if let dealsData = reply["deals"] as? [[String: Any]], !dealsData.isEmpty {
            self.deals = parseDeals(from: dealsData)
            let source = reply["source"] as? String ?? "iPhone 17"
            self.syncSource = "⚡ \(source)"
        } else {
            fetchDirectlyFromApi()
        }
    }

    private func parseApiCourses(from courses: [[String: Any]]) -> [WatchDealModel] {
        return courses.compactMap { dict -> WatchDealModel? in
            guard let title = dict["title"] as? String,
                  let expiredTime = (dict["expiredTime"] as? Double) ?? (dict["expiredTime"] as? Int64).map(Double.init) else {
                return nil
            }

            let idVal = (dict["courseId"] as? Int) ?? 501
            let author = dict["author"] as? String ?? "Course Deals API"
            let category = dict["category"] as? String ?? "Development"
            let code = dict["couponCode"] as? String ?? "LIVEAPI"
            let url = dict["targetUrl"] as? String ?? "https://coursedeals.com"
            let rating = dict["rating"] as? Double ?? 4.95

            let platform: WatchCoursePlatform
            if title.contains("Swift") {
                platform = .udemy
            } else if title.contains("PyTorch") {
                platform = .coursera
            } else {
                platform = .edx
            }

            return WatchDealModel(
                id: idVal,
                title: title,
                heading: nil,
                author: author,
                category: category,
                couponCode: code,
                couponUrl: url,
                expiredTime: Int64(expiredTime),
                rating: rating,
                reviews: 240,
                usesRemaining: 20,
                platform: platform,
                isBookmarked: false
            )
        }
    }

    private func parseDeals(from data: [[String: Any]]) -> [WatchDealModel] {
        return data.compactMap { (dict: [String: Any]) -> WatchDealModel? in
            guard let title = dict["title"] as? String,
                  let platformStr = dict["platform"] as? String,
                  let expiresAt = dict["expiresAtEpochSeconds"] as? Double else {
                return nil
            }

            let idInt = (dict["id"] as? Int) ?? Int((dict["id"] as? String) ?? "500") ?? 500
            let platform: WatchCoursePlatform
            switch platformStr.lowercased() {
            case "udemy": platform = .udemy
            case "coursera": platform = .coursera
            case "edx": platform = .edx
            default: platform = .udemy
            }

            return WatchDealModel(
                id: idInt,
                title: title,
                heading: nil,
                author: "Course Deals API",
                category: "Mobile",
                couponCode: dict["couponCode"] as? String ?? "LIVEAPI",
                couponUrl: dict["targetUrl"] as? String ?? "https://coursedeals.com",
                expiredTime: Int64(expiresAt),
                rating: dict["rating"] as? Double ?? 4.95,
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
            if let dealsData = applicationContext["deals"] as? [[String: Any]], !dealsData.isEmpty {
                self.deals = self.parseDeals(from: dealsData)
                let source = applicationContext["source"] as? String ?? "iPhone Context"
                self.syncSource = "⚡ \(source)"
            }
        }
    }

    public func session(_ session: WCSession, didReceiveMessage message: [String : Any]) {
        DispatchQueue.main.async {
            if let dealsData = message["deals"] as? [[String: Any]], !dealsData.isEmpty {
                self.deals = self.parseDeals(from: dealsData)
                let source = message["source"] as? String ?? "iPhone Push"
                self.syncSource = "⚡ \(source)"
            }
        }
    }
}
