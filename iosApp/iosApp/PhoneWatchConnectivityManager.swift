import Foundation
import WatchConnectivity
#if canImport(ComposeApp)
import ComposeApp
#endif

public class PhoneWatchConnectivityManager: NSObject, WCSessionDelegate, ObservableObject {
    public static let shared = PhoneWatchConnectivityManager()

    public static let watchBookmarksKey = "watch_bookmarked_course_ids"
    public static let handoffActivityType = "com.thanh0x.coursedeals.courseDetail"
    public static let legacyHandoffActivityType = "com.coursedeals.deal.open"
    public static let watchToPhoneEventFilePath = "/tmp/coursedeals_watch_to_phone_event.json"
    public static let phoneBookmarksFilePath = "/tmp/coursedeals_phone_bookmarks.json"

    @Published public var isWatchPaired: Bool = false
    @Published public var isWatchAppInstalled: Bool = false
    @Published public var userPreference: String? = nil
    @Published public var pendingDeepLinkUrl: String? = nil
    @Published public var bookmarkedCourseIds: Set<Int> = []

    private var lastProcessedEventId: String? = nil
    private var simulatorRelayTimer: Timer? = nil

    public override init() {
        super.init()
        self.bookmarkedCourseIds = Self.loadStoredBookmarkedIds()
        Self.writeSharedPhoneBookmarks(self.bookmarkedCourseIds)

        if WCSession.isSupported() {
            let session = WCSession.default
            session.delegate = self
            session.activate()
        }

        startSimulatorEventRelayTimer()
    }

    private func startSimulatorEventRelayTimer() {
        DispatchQueue.main.async { [weak self] in
            self?.simulatorRelayTimer?.invalidate()
            let timer = Timer(timeInterval: 0.4, repeats: true) { [weak self] _ in
                self?.pollWatchToPhoneEventFile()
            }
            RunLoop.main.add(timer, forMode: .common)
            self?.simulatorRelayTimer = timer
        }
    }

    private func pollWatchToPhoneEventFile() {
        let url = URL(fileURLWithPath: Self.watchToPhoneEventFilePath)
        guard let data = try? Data(contentsOf: url),
              let dict = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let eventId = dict["eventId"] as? String,
              !eventId.isEmpty,
              eventId != lastProcessedEventId else {
            return
        }

        let timestamp = (dict["timestamp"] as? TimeInterval)
            ?? (dict["timestamp"] as? NSNumber)?.doubleValue
            ?? 0
        guard timestamp > Date().timeIntervalSince1970 - 30 else {
            return
        }

        lastProcessedEventId = eventId
        _ = handleIncomingWatchAction(dict)
    }

    public static func loadStoredBookmarkedIds(defaults: UserDefaults = .standard) -> Set<Int> {
        var result = Set<Int>()
        if let csv = defaults.string(forKey: watchBookmarksKey) {
            let parsed = csv
                .split(separator: ",")
                .compactMap { Int($0.trimmingCharacters(in: .whitespacesAndNewlines)) }
                .filter { $0 > 0 }
            result.formUnion(parsed)
        }
        if let intArray = defaults.array(forKey: watchBookmarksKey) as? [Int] {
            result.formUnion(intArray.filter { $0 > 0 })
        }
        if let numArray = defaults.array(forKey: watchBookmarksKey) as? [NSNumber] {
            result.formUnion(numArray.map { $0.intValue }.filter { $0 > 0 })
        }
        let sharedUrl = URL(fileURLWithPath: phoneBookmarksFilePath)
        if let data = try? Data(contentsOf: sharedUrl),
           let sharedIds = try? JSONDecoder().decode([Int].self, from: data) {
            result.formUnion(sharedIds.filter { $0 > 0 })
        }
        return result
    }

    private static func writeSharedPhoneBookmarks(_ ids: Set<Int>) {
        let sorted = ids.filter { $0 > 0 }.sorted()
        guard let data = try? JSONEncoder().encode(sorted) else { return }
        try? data.write(to: URL(fileURLWithPath: phoneBookmarksFilePath), options: .atomic)
    }

    private func persistBookmarkedIds(_ ids: Set<Int>) {
        let sorted = ids.filter { $0 > 0 }.sorted()
        let csv = sorted.map(String.init).joined(separator: ",")
        UserDefaults.standard.set(csv, forKey: Self.watchBookmarksKey)
        Self.writeSharedPhoneBookmarks(ids)
    }

    public func buildHotDealsUrl(preference: String? = nil) -> URL {
        let pref = preference ?? userPreference
        #if canImport(ComposeApp)
        let urlString = MainViewControllerKt.buildWatchHotDealsUrl(preference: pref)
        if let url = URL(string: urlString) {
            return url
        }
        #endif
        return URL(string: "http://127.0.0.1:8081/api/v1/coupons/hot-deals?pageIndex=0&numberPerPage=20&minRating=4.7")!
    }

    public func fetchLiveDealsFromAPI(preference: String? = nil, completion: @escaping ([[String: Any]]) -> Void) {
        let url = buildHotDealsUrl(preference: preference)
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.timeoutInterval = 5.0
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let currentBookmarks = Self.loadStoredBookmarkedIds().union(bookmarkedCourseIds)
        let bookmarksCsv = currentBookmarks.sorted().map(String.init).joined(separator: ",")

        let task = URLSession.shared.dataTask(with: request) { data, _, error in
            guard let data = data, error == nil, let rawJson = String(data: data, encoding: .utf8) else {
                print("[PhoneWatchConnectivity] Live API error: \(String(describing: error))")
                completion([])
                return
            }

            #if canImport(ComposeApp)
            let dealsJson = MainViewControllerKt.mapHotDealsJsonForWatch(
                apiJson: rawJson,
                bookmarkedIdsCsv: bookmarksCsv
            )
            if let mappedData = dealsJson.data(using: .utf8),
               let mappedDeals = (try? JSONSerialization.jsonObject(with: mappedData)) as? [[String: Any]] {
                completion(mappedDeals)
                return
            }
            #endif
            completion([])
        }
        task.resume()
    }

    public func syncDealsToWatch(deals: [[String: Any]], preference: String? = nil) {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        guard session.activationState == .activated else { return }

        let bookmarks = Self.loadStoredBookmarkedIds().union(bookmarkedCourseIds)
        let dealsJson = (try? JSONSerialization.data(withJSONObject: deals))
            .flatMap { String(data: $0, encoding: .utf8) } ?? "[]"
        var payload: [String: Any] = [
            "deals": deals,
            "dealsJson": dealsJson,
            "bookmarkedIds": Array(bookmarks).sorted(),
            "timestamp": Date().timeIntervalSince1970,
            "source": "Spring Boot :8081 via iPhone"
        ]
        if let pref = preference ?? userPreference {
            payload["preference"] = pref
        }

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

    public func pushLatestDeals(preference: String? = nil) {
        let pref = preference ?? userPreference
        fetchLiveDealsFromAPI(preference: pref) { [weak self] liveDeals in
            if !liveDeals.isEmpty {
                self?.syncDealsToWatch(deals: liveDeals, preference: pref)
            }
        }
    }

    // MARK: - Handoff & Watch Event Routing

    public func handleUserActivity(_ userActivity: NSUserActivity) {
        let type = userActivity.activityType
        guard type == Self.handoffActivityType || type == Self.legacyHandoffActivityType else {
            return
        }

        let info = userActivity.userInfo ?? [:]
        let courseId = Self.extractCourseId(from: info)
        let deepLink = (info["deepLink"] as? String)
            ?? (courseId > 0 ? "coursedeals://courses/\(courseId)" : nil)
            ?? userActivity.webpageURL?.absoluteString

        DispatchQueue.main.async {
            if let deepLink = deepLink {
                self.pendingDeepLinkUrl = deepLink
                #if canImport(ComposeApp)
                MainViewControllerKt.handleDeepLink(url: deepLink)
                #endif
            } else if courseId > 0 {
                self.pendingDeepLinkUrl = "coursedeals://courses/\(courseId)"
                #if canImport(ComposeApp)
                MainViewControllerKt.handleHandoffCourse(courseId: Int32(courseId))
                #endif
            }
        }
    }

    @discardableResult
    public func handleIncomingWatchAction(_ message: [String: Any]) -> Bool {
        let action = (message["action"] as? String) ?? (message["request"] as? String) ?? ""

        if action == "bookmarkDeal" {
            let courseId = Self.extractCourseId(from: message)
            let isBookmarked = (message["isBookmarked"] as? Bool)
                ?? (message["isBookmarked"] as? NSNumber)?.boolValue
                ?? true
            guard courseId > 0 else { return true }

            DispatchQueue.main.async {
                var updated = Self.loadStoredBookmarkedIds().union(self.bookmarkedCourseIds)
                if isBookmarked {
                    updated.insert(courseId)
                } else {
                    updated.remove(courseId)
                }
                self.bookmarkedCourseIds = updated
                self.persistBookmarkedIds(updated)
                #if canImport(ComposeApp)
                MainViewControllerKt.syncWatchBookmark(
                    courseId: Int32(courseId),
                    isBookmarked: isBookmarked
                )
                #endif
            }
            return true
        }

        if action == "openCourseOnPhone" {
            let courseId = Self.extractCourseId(from: message)
            let deepLink = (message["deepLink"] as? String)
                ?? (courseId > 0 ? "coursedeals://courses/\(courseId)" : nil)

            DispatchQueue.main.async {
                if let deepLink = deepLink {
                    self.pendingDeepLinkUrl = deepLink
                }
                #if canImport(ComposeApp)
                if courseId > 0 {
                    MainViewControllerKt.handleHandoffCourse(courseId: Int32(courseId))
                } else if let deepLink = deepLink {
                    MainViewControllerKt.handleDeepLink(url: deepLink)
                }
                #endif
            }
            return true
        }

        return false
    }

    private static func extractCourseId(from dict: [AnyHashable: Any]) -> Int {
        if let id = dict["courseId"] as? Int, id > 0 { return id }
        if let num = dict["courseId"] as? NSNumber, num.intValue > 0 { return num.intValue }
        if let str = dict["courseId"] as? String, let id = Int(str), id > 0 { return id }
        if let id = dict["dealId"] as? Int, id > 0 { return id }
        if let num = dict["dealId"] as? NSNumber, num.intValue > 0 { return num.intValue }
        if let str = dict["dealId"] as? String, let id = Int(str), id > 0 { return id }
        return 0
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

    public func session(_ session: WCSession, didReceiveMessage message: [String: Any]) {
        _ = handleIncomingWatchAction(message)
    }

    public func session(_ session: WCSession, didReceiveUserInfo userInfo: [String: Any] = [:]) {
        _ = handleIncomingWatchAction(userInfo)
    }

    public func session(_ session: WCSession, didReceiveMessage message: [String: Any], replyHandler: @escaping ([String: Any]) -> Void) {
        if handleIncomingWatchAction(message) {
            let bookmarks = Self.loadStoredBookmarkedIds().union(bookmarkedCourseIds)
            replyHandler([
                "status": "ok",
                "bookmarkedIds": Array(bookmarks).sorted()
            ])
            return
        }

        if message["request"] as? String == "fetchDeals" {
            let pref = (message["preference"] as? String) ?? userPreference
            let bookmarks = Self.loadStoredBookmarkedIds().union(bookmarkedCourseIds)
            fetchLiveDealsFromAPI(preference: pref) { liveDeals in
                if !liveDeals.isEmpty {
                    let dealsJson = (try? JSONSerialization.data(withJSONObject: liveDeals))
                        .flatMap { String(data: $0, encoding: .utf8) } ?? "[]"
                    var reply: [String: Any] = [
                        "status": "success",
                        "deals": liveDeals,
                        "dealsJson": dealsJson,
                        "bookmarkedIds": Array(bookmarks).sorted(),
                        "source": "Spring Boot DB via iPhone"
                    ]
                    if let pref = pref {
                        reply["preference"] = pref
                    }
                    replyHandler(reply)
                } else {
                    replyHandler([
                        "status": "error",
                        "deals": [],
                        "dealsJson": "[]",
                        "bookmarkedIds": Array(bookmarks).sorted(),
                        "source": "API Unreachable"
                    ])
                }
            }
        }
    }
}
