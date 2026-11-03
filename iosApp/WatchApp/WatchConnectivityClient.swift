import Foundation
import SwiftUI
import WatchConnectivity

public class WatchConnectivityClient: NSObject, WCSessionDelegate, ObservableObject {
    public static let shared = WatchConnectivityClient()

    @Published public var deals: [WatchDealModel] = []
    @Published public var isConnectedToPhone: Bool = false
    @Published public var syncSource: String = "Connecting..."
    @Published public var isLoading: Bool = false
    @Published public var userPreference: String? = nil

    public override init() {
        super.init()
        if WCSession.isSupported() {
            let session = WCSession.default
            session.delegate = self
            session.activate()
        }
        self.deals = []
        requestDealsFromPhone()
    }

    public func buildHotDealsUrl(preference: String? = nil) -> URL {
        var components = URLComponents(string: "http://127.0.0.1:8081/api/v1/coupons/hot-deals")!
        var queryItems = [
            URLQueryItem(name: "pageIndex", value: "0"),
            URLQueryItem(name: "numberPerPage", value: "20"),
            URLQueryItem(name: "minRating", value: "4.7")
        ]
        let pref = preference ?? userPreference
        if let pref = pref, !pref.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            queryItems.append(URLQueryItem(name: "preference", value: pref))
        }
        components.queryItems = queryItems
        return components.url ?? URL(string: "http://127.0.0.1:8081/api/v1/coupons/hot-deals")!
    }

    public func requestDealsFromPhone(preference: String? = nil) {
        if let pref = preference {
            self.userPreference = pref
        }
        self.isLoading = true
        self.syncSource = "Fetching Hot Deals..."

        let session = WCSession.default
        var message: [String: Any] = ["request": "fetchDeals"]
        if let pref = preference ?? userPreference, !pref.isEmpty {
            message["preference"] = pref
        }

        if WCSession.isSupported() && session.activationState == .activated && session.isReachable {
            session.sendMessage(message) { [weak self] reply in
                DispatchQueue.main.async {
                    self?.handlePhoneReply(reply)
                }
            } errorHandler: { [weak self] _ in
                DispatchQueue.main.async {
                    // Fallback to direct network fetch
                    self?.fetchDirectlyFromApi(preference: preference)
                }
            }
        } else {
            // Check application context or fetch directly from API
            if let pref = session.receivedApplicationContext["preference"] as? String {
                self.userPreference = pref
            }
            if let contextDeals = session.receivedApplicationContext["deals"] as? [[String: Any]], !contextDeals.isEmpty {
                self.deals = self.parseDeals(from: contextDeals)
                self.syncSource = "⚡ Synced via iPhone Context"
                self.isLoading = false
            } else {
                fetchDirectlyFromApi(preference: preference)
            }
        }
    }

    public func fetchDirectlyFromApi(preference: String? = nil) {
        let url = buildHotDealsUrl(preference: preference)
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.timeoutInterval = 4.0
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let task = URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                guard let data = data, error == nil else {
                    if self?.deals.isEmpty ?? true {
                        self?.syncSource = "Offline (API Unreachable)"
                    }
                    return
                }

                do {
                    if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
                       let courses = json["courses"] as? [[String: Any]], !courses.isEmpty {
                        self?.deals = self?.parseApiCourses(from: courses) ?? []
                        self?.syncSource = "🔥 Hot Deals :8081"
                        return
                    } else if (self?.deals.isEmpty ?? true) {
                        self?.syncSource = "No Hot Deals Found"
                    }
                } catch {
                    if (self?.deals.isEmpty ?? true) {
                        self?.syncSource = "API Parse Error"
                    }
                }
            }
        }
        task.resume()
    }

    private func handlePhoneReply(_ reply: [String: Any]) {
        self.isLoading = false
        if let pref = reply["preference"] as? String {
            self.userPreference = pref
        }
        if let dealsData = reply["deals"] as? [[String: Any]], !dealsData.isEmpty {
            self.deals = parseDeals(from: dealsData)
            let source = reply["source"] as? String ?? "iPhone 17"
            self.syncSource = "⚡ \(source)"
        } else {
            fetchDirectlyFromApi(preference: self.userPreference)
        }
    }

    private func parseApiCourses(from courses: [[String: Any]]) -> [WatchDealModel] {
        return courses.compactMap { dict -> WatchDealModel? in
            guard let title = dict["title"] as? String else {
                return nil
            }

            let expiredTime = (dict["expiredTime"] as? Double)
                ?? (dict["expiredTime"] as? Int64).map(Double.init)
                ?? (dict["expiredTime"] as? NSNumber)?.doubleValue

            let idVal = (dict["courseId"] as? Int)
                ?? (dict["courseId"] as? NSNumber)?.intValue
                ?? 501

            let author = dict["author"] as? String ?? "Udemy Instructor"
            let category = dict["category"] as? String ?? "Development"
            let code = dict["couponCode"] as? String ?? "HOT100"
            let url = dict["targetUrl"] as? String ?? dict["previewImage"] as? String ?? "https://coursedeals.com"
            let rating = (dict["rating"] as? Double) ?? (dict["rating"] as? NSNumber)?.doubleValue ?? 4.8
            let reviews = (dict["reviews"] as? Int) ?? (dict["reviews"] as? NSNumber)?.intValue ?? 240
            let students = (dict["students"] as? Int) ?? (dict["students"] as? NSNumber)?.intValue ?? 0

            let previewImage = dict["previewImage"] as? String ?? ""
            let platform: WatchCoursePlatform
            if previewImage.contains("coursera") || title.lowercased().contains("coursera") {
                platform = .coursera
            } else if previewImage.contains("edx") || title.lowercased().contains("edx") {
                platform = .edx
            } else {
                platform = .udemy
            }

            return WatchDealModel(
                id: idVal,
                title: title,
                heading: nil,
                author: author,
                category: category,
                couponCode: code,
                couponUrl: url,
                expiredTime: expiredTime.map { Int64($0) },
                rating: rating,
                reviews: reviews,
                usesRemaining: max(5, reviews / 20),
                platform: platform,
                isBookmarked: false,
                students: students,
                isHot: true
            )
        }
    }

    private func parseDeals(from data: [[String: Any]]) -> [WatchDealModel] {
        return data.compactMap { (dict: [String: Any]) -> WatchDealModel? in
            guard let title = dict["title"] as? String else {
                return nil
            }

            let idInt = (dict["id"] as? Int)
                ?? Int((dict["id"] as? String) ?? "500")
                ?? (dict["courseId"] as? Int)
                ?? 500

            let platformStr = dict["platform"] as? String ?? "Udemy"
            let platform: WatchCoursePlatform
            switch platformStr.lowercased() {
            case "udemy": platform = .udemy
            case "coursera": platform = .coursera
            case "edx": platform = .edx
            default: platform = .udemy
            }

            let expiresAt = (dict["expiresAtEpochSeconds"] as? Double)
                ?? (dict["expiredTime"] as? Double)
                ?? (dict["expiredTime"] as? Int64).map(Double.init)

            let rating = (dict["rating"] as? Double) ?? (dict["rating"] as? NSNumber)?.doubleValue ?? 4.8
            let reviews = (dict["reviews"] as? Int) ?? (dict["reviews"] as? NSNumber)?.intValue ?? 120
            let students = (dict["students"] as? Int) ?? (dict["students"] as? NSNumber)?.intValue
            let isHot = (dict["isHot"] as? Bool) ?? true

            return WatchDealModel(
                id: idInt,
                title: title,
                heading: nil,
                author: dict["author"] as? String ?? "Course Deals API",
                category: dict["category"] as? String ?? "Mobile",
                couponCode: dict["couponCode"] as? String ?? "HOT100",
                couponUrl: dict["targetUrl"] as? String ?? "https://coursedeals.com",
                expiredTime: expiresAt.map { Int64($0) },
                rating: rating,
                reviews: reviews,
                usesRemaining: dict["usesLeft"] as? Int ?? max(5, reviews / 20),
                platform: platform,
                isBookmarked: false,
                students: students,
                isHot: isHot
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
            if let pref = applicationContext["preference"] as? String {
                self.userPreference = pref
            }
            if let dealsData = applicationContext["deals"] as? [[String: Any]], !dealsData.isEmpty {
                self.deals = self.parseDeals(from: dealsData)
                let source = applicationContext["source"] as? String ?? "iPhone Context"
                self.syncSource = "⚡ \(source)"
            }
        }
    }

    public func session(_ session: WCSession, didReceiveMessage message: [String : Any]) {
        DispatchQueue.main.async {
            if let pref = message["preference"] as? String {
                self.userPreference = pref
            }
            if let dealsData = message["deals"] as? [[String: Any]], !dealsData.isEmpty {
                self.deals = self.parseDeals(from: dealsData)
                let source = message["source"] as? String ?? "iPhone Push"
                self.syncSource = "⚡ \(source)"
            }
        }
    }
}
