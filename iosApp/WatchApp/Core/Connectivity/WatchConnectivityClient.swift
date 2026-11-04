import Foundation
import SwiftUI
import CourseDealsWatch
#if canImport(WatchConnectivity)
import WatchConnectivity
#endif
#if canImport(WidgetKit)
import WidgetKit
#endif

public class WatchConnectivityClient: NSObject, ObservableObject {
    public static let shared = WatchConnectivityClient()
    public static let watchToPhoneEventFilePath = "/tmp/coursedeals_watch_to_phone_event.json"
    public static let phoneBookmarksFilePath = "/tmp/coursedeals_phone_bookmarks.json"

    @Published public var deals: [WatchDealModel] = []
    @Published public var isConnectedToPhone: Bool = false
    @Published public var syncSource: String = "Connecting..."
    @Published public var isLoading: Bool = false
    @Published public var userPreference: String? = nil

    public let cacheStore: WatchDealCacheStore

    public init(cacheStore: WatchDealCacheStore = .shared) {
        self.cacheStore = cacheStore
        super.init()

        // 1. Instant offline hydration from App Group / UserDefaults / shared cache before WCSession handshake
        let cachedDeals = cacheStore.loadDeals()
        if !cachedDeals.isEmpty {
            self.deals = cachedDeals
            self.syncSource = "📦 Offline Cache"
            self.isLoading = false
        }

        #if canImport(WatchConnectivity)
        if WCSession.isSupported() {
            let session = WCSession.default
            session.delegate = self
            session.activate()
        }
        #endif
        requestDealsFromPhone()
    }

    public func buildHotDealsUrl(preference: String? = nil) -> URL {
        let urlString = WatchDealJsonCodec.shared.buildHotDealsUrl(
            baseUrl: "http://127.0.0.1:8081",
            preference: preference ?? userPreference
        )
        return URL(string: urlString) ?? URL(string: "http://127.0.0.1:8081/api/v1/coupons/hot-deals")!
    }

    public func requestDealsFromPhone(preference: String? = nil) {
        if let pref = preference {
            self.userPreference = pref
        }
        if self.deals.isEmpty {
            self.isLoading = true
            self.syncSource = "Fetching Hot Deals..."
        }

        #if canImport(WatchConnectivity)
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
                    self?.fetchDirectlyFromApi(preference: preference)
                }
            }
        } else {
            if WCSession.isSupported() && session.activationState == .activated {
                if let pref = session.receivedApplicationContext["preference"] as? String {
                    self.userPreference = pref
                }
                let remoteBookmarks = extractBookmarkedIds(from: session.receivedApplicationContext)
                let parsed = decodeDealsFromPayload(session.receivedApplicationContext)
                if !parsed.isEmpty {
                    applyAndPersistDeals(
                        parsed,
                        remoteBookmarkedIds: remoteBookmarks,
                        sourceLabel: "⚡ Synced via iPhone Context"
                    )
                    return
                }
            }
            fetchDirectlyFromApi(preference: preference)
        }
        #else
        fetchDirectlyFromApi(preference: preference)
        #endif
    }

    public func fetchDirectlyFromApi(preference: String? = nil) {
        let url = buildHotDealsUrl(preference: preference)
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.timeoutInterval = 4.0
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let task = URLSession.shared.dataTask(with: request) { [weak self] data, _, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                self.isLoading = false
                guard let data = data, error == nil else {
                    if self.deals.isEmpty {
                        let fallbackCached = self.cacheStore.loadDeals()
                        if !fallbackCached.isEmpty {
                            self.deals = fallbackCached
                            self.syncSource = "📦 Cached (Offline)"
                        } else {
                            self.syncSource = "Offline (API Unreachable)"
                        }
                    } else {
                        self.syncSource = "📦 Cached (Offline)"
                    }
                    return
                }

                let parsed = WatchHotDealsEnvelope.decodeDeals(from: data)
                if !parsed.isEmpty {
                    let sharedPhoneBookmarks = self.readSharedPhoneBookmarks()
                    self.applyAndPersistDeals(
                        parsed,
                        remoteBookmarkedIds: sharedPhoneBookmarks,
                        sourceLabel: "🔥 Hot Deals :8081"
                    )
                } else if self.deals.isEmpty {
                    self.syncSource = "No Hot Deals Found"
                }
            }
        }
        task.resume()
    }

    // MARK: - Bookmark & Handoff Actions

    @discardableResult
    public func toggleBookmark(for dealId: Int32) -> Bool {
        toggleBookmark(for: Int(dealId))
    }

    @discardableResult
    public func toggleBookmark(for dealId: Int) -> Bool {
        let currentlyBookmarked = deals.first(where: { $0.courseId == dealId })?.isBookmarked
            ?? cacheStore.loadBookmarkedIds().contains(dealId)
        let newState = !currentlyBookmarked

        let updatedBookmarks = cacheStore.setBookmarked(dealId: dealId, isBookmarked: newState)
        for index in deals.indices where deals[index].courseId == dealId {
            deals[index] = deals[index].withBookmark(bookmarked: newState)
        }
        cacheStore.saveDeals(deals)
        writeSharedPhoneBookmarks(updatedBookmarks)
        reloadWidgetTimelines()

        let payload: [String: Any] = [
            "action": "bookmarkDeal",
            "request": "bookmarkDeal",
            "courseId": dealId,
            "isBookmarked": newState,
            "timestamp": Date().timeIntervalSince1970
        ]
        sendPayloadToPhone(payload)
        return newState
    }

    public func sendOpenOnPhoneRequest(for deal: WatchDealModel) {
        let payload: [String: Any] = [
            "action": "openCourseOnPhone",
            "request": "openCourseOnPhone",
            "courseId": deal.courseId,
            "dealId": deal.courseId,
            "deepLink": deal.deepLinkUrl.absoluteString,
            "title": deal.title,
            "platform": deal.platform.displayName,
            "timestamp": Date().timeIntervalSince1970
        ]
        sendPayloadToPhone(payload)
    }

    private func sendPayloadToPhone(_ payload: [String: Any]) {
        var enrichedPayload = payload
        let eventId = UUID().uuidString
        enrichedPayload["eventId"] = eventId
        if enrichedPayload["timestamp"] == nil {
            enrichedPayload["timestamp"] = Date().timeIntervalSince1970
        }

        if JSONSerialization.isValidJSONObject(enrichedPayload),
           let data = try? JSONSerialization.data(withJSONObject: enrichedPayload, options: []) {
            try? data.write(
                to: URL(fileURLWithPath: Self.watchToPhoneEventFilePath),
                options: .atomic
            )
        }

        #if canImport(WatchConnectivity)
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        guard session.activationState == .activated else { return }

        if session.isReachable {
            session.sendMessage(enrichedPayload, replyHandler: nil) { _ in
                session.transferUserInfo(enrichedPayload)
            }
        } else {
            session.transferUserInfo(enrichedPayload)
        }
        #endif
    }

    private func readSharedPhoneBookmarks() -> Set<Int> {
        let url = URL(fileURLWithPath: Self.phoneBookmarksFilePath)
        guard let data = try? Data(contentsOf: url),
              let ids = try? JSONDecoder().decode([Int].self, from: data) else {
            return []
        }
        return Set(ids.filter { $0 > 0 })
    }

    private func writeSharedPhoneBookmarks(_ ids: Set<Int>) {
        let sorted = ids.filter { $0 > 0 }.sorted()
        guard let data = try? JSONEncoder().encode(sorted) else { return }
        try? data.write(to: URL(fileURLWithPath: Self.phoneBookmarksFilePath), options: .atomic)
    }

    internal func applyAndPersistDeals(
        _ incomingDeals: [WatchDealModel],
        remoteBookmarkedIds: Set<Int>,
        sourceLabel: String
    ) {
        let merged = cacheStore.mergeDealsWithBookmarks(
            incomingDeals,
            extraBookmarkedIds: remoteBookmarkedIds
        )
        self.deals = merged
        self.syncSource = sourceLabel
        self.isLoading = false
        cacheStore.saveDeals(merged)
        reloadWidgetTimelines()
    }

    private func reloadWidgetTimelines() {
        #if canImport(WidgetKit)
        WidgetCenter.shared.reloadAllTimelines()
        #endif
    }

    internal func extractBookmarkedIds(from payload: [String: Any]) -> Set<Int> {
        if let intArray = payload["bookmarkedIds"] as? [Int] {
            return Set(intArray.filter { $0 > 0 })
        }
        if let numArray = payload["bookmarkedIds"] as? [NSNumber] {
            return Set(numArray.map { $0.intValue }.filter { $0 > 0 })
        }
        return []
    }

    internal func decodeDealsFromPayload(_ payload: [String: Any]) -> [WatchDealModel] {
        if let dealsJson = payload["dealsJson"] as? String,
           let data = dealsJson.data(using: .utf8) {
            let decoded = WatchHotDealsEnvelope.decodeDeals(from: data)
            if !decoded.isEmpty {
                return decoded
            }
        }
        if let dealsObj = payload["deals"],
           JSONSerialization.isValidJSONObject(dealsObj),
           let data = try? JSONSerialization.data(withJSONObject: dealsObj, options: []) {
            return WatchHotDealsEnvelope.decodeDeals(from: data)
        }
        return []
    }

    internal func handlePhoneReply(_ reply: [String: Any]) {
        self.isLoading = false
        if let pref = reply["preference"] as? String {
            self.userPreference = pref
        }
        let remoteBookmarks = extractBookmarkedIds(from: reply)
        let parsed = decodeDealsFromPayload(reply)
        if !parsed.isEmpty {
            let source = reply["source"] as? String ?? "iPhone 17"
            applyAndPersistDeals(parsed, remoteBookmarkedIds: remoteBookmarks, sourceLabel: "⚡ \(source)")
        } else {
            fetchDirectlyFromApi(preference: self.userPreference)
        }
    }
}

#if canImport(WatchConnectivity)
extension WatchConnectivityClient: WCSessionDelegate {
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

    public func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        DispatchQueue.main.async {
            if let pref = applicationContext["preference"] as? String {
                self.userPreference = pref
            }
            let remoteBookmarks = self.extractBookmarkedIds(from: applicationContext)
            let parsed = self.decodeDealsFromPayload(applicationContext)
            if !parsed.isEmpty {
                let source = applicationContext["source"] as? String ?? "iPhone Context"
                self.applyAndPersistDeals(parsed, remoteBookmarkedIds: remoteBookmarks, sourceLabel: "⚡ \(source)")
            }
        }
    }

    public func session(_ session: WCSession, didReceiveMessage message: [String: Any]) {
        DispatchQueue.main.async {
            if let pref = message["preference"] as? String {
                self.userPreference = pref
            }
            let remoteBookmarks = self.extractBookmarkedIds(from: message)
            let parsed = self.decodeDealsFromPayload(message)
            if !parsed.isEmpty {
                let source = message["source"] as? String ?? "iPhone Push"
                self.applyAndPersistDeals(parsed, remoteBookmarkedIds: remoteBookmarks, sourceLabel: "⚡ \(source)")
            }
        }
    }
}
#endif
