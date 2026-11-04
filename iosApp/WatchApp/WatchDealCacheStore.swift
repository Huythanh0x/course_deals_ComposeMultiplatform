import Foundation

private struct SharedWatchCachePayload: Codable {
    let deals: [WatchDealModel]
    let bookmarkedIds: [Int]
    let timestamp: TimeInterval
}

/// Persistent offline cache store for Apple Watch deals and bookmarked course IDs.
/// Uses App Group `group.com.thanh0x.coursedeals` with automatic fallback/mirroring to `.standard`
/// and shared file `/tmp/coursedeals_watch_shared_cache_v1.json` so both `WatchApp` and `WatchWidgetsExtension.appex`
/// read consistent state.
public final class WatchDealCacheStore {
    public static let shared = WatchDealCacheStore()

    public static let appGroupSuiteName = "group.com.thanh0x.coursedeals"
    public static let cachedDealsKey = "cached_watch_deals_v1"
    public static let bookmarkedIdsKey = "bookmarked_watch_deal_ids_v1"
    public static let lastCacheTimestampKey = "last_cache_timestamp_v1"
    public static let sharedCacheFilePath = "/tmp/coursedeals_watch_shared_cache_v1.json"

    private let primaryDefaults: UserDefaults
    private let fallbackDefaults: UserDefaults
    private let mirrorToStandard: Bool

    public init(userDefaults: UserDefaults? = nil, mirrorToStandard: Bool = true) {
        if let custom = userDefaults {
            self.primaryDefaults = custom
        } else {
            self.primaryDefaults = UserDefaults(suiteName: Self.appGroupSuiteName) ?? .standard
        }
        self.fallbackDefaults = .standard
        self.mirrorToStandard = mirrorToStandard
    }

    public func saveDeals(_ deals: [WatchDealModel]) {
        var bookmarked = loadBookmarkedIds()
        for deal in deals where deal.isBookmarked {
            bookmarked.insert(deal.id)
        }
        saveBookmarkedIds(bookmarked)

        let normalizedDeals = deals.map { deal -> WatchDealModel in
            var copy = deal
            copy.isBookmarked = bookmarked.contains(deal.id)
            return copy
        }

        guard let encoded = try? JSONEncoder().encode(normalizedDeals) else {
            return
        }
        let now = Date().timeIntervalSince1970
        primaryDefaults.set(encoded, forKey: Self.cachedDealsKey)
        primaryDefaults.set(now, forKey: Self.lastCacheTimestampKey)

        if mirrorToStandard {
            if primaryDefaults !== fallbackDefaults {
                fallbackDefaults.set(encoded, forKey: Self.cachedDealsKey)
                fallbackDefaults.set(now, forKey: Self.lastCacheTimestampKey)
            }
            writeSharedCacheFile(deals: normalizedDeals, bookmarkedIds: bookmarked, timestamp: now)
        }
    }

    public func loadDeals() -> [WatchDealModel] {
        let rawData = primaryDefaults.data(forKey: Self.cachedDealsKey)
            ?? (mirrorToStandard ? fallbackDefaults.data(forKey: Self.cachedDealsKey) : nil)

        var decodedDeals: [WatchDealModel] = []
        if let data = rawData,
           let decoded = try? JSONDecoder().decode([WatchDealModel].self, from: data) {
            decodedDeals = decoded
        } else if mirrorToStandard, let sharedPayload = readSharedCacheFile() {
            decodedDeals = sharedPayload.deals
        }

        guard !decodedDeals.isEmpty else {
            return []
        }

        let bookmarked = loadBookmarkedIds()
        return decodedDeals.map { deal in
            var copy = deal
            if bookmarked.contains(deal.id) {
                copy.isBookmarked = true
            }
            return copy
        }
    }

    public func loadBookmarkedIds() -> Set<Int> {
        if let array = primaryDefaults.array(forKey: Self.bookmarkedIdsKey) as? [Int] {
            return Set(array.filter { $0 > 0 })
        }
        if let nsNumbers = primaryDefaults.array(forKey: Self.bookmarkedIdsKey) as? [NSNumber] {
            return Set(nsNumbers.map { $0.intValue }.filter { $0 > 0 })
        }
        if mirrorToStandard {
            if let array = fallbackDefaults.array(forKey: Self.bookmarkedIdsKey) as? [Int], !array.isEmpty {
                return Set(array.filter { $0 > 0 })
            }
            if let nsNumbers = fallbackDefaults.array(forKey: Self.bookmarkedIdsKey) as? [NSNumber], !nsNumbers.isEmpty {
                return Set(nsNumbers.map { $0.intValue }.filter { $0 > 0 })
            }
            if let sharedPayload = readSharedCacheFile() {
                return Set(sharedPayload.bookmarkedIds.filter { $0 > 0 })
            }
        }
        return []
    }

    public func saveBookmarkedIds(_ ids: Set<Int>) {
        let sorted = ids.filter { $0 > 0 }.sorted()
        primaryDefaults.set(sorted, forKey: Self.bookmarkedIdsKey)
        if mirrorToStandard && primaryDefaults !== fallbackDefaults {
            fallbackDefaults.set(sorted, forKey: Self.bookmarkedIdsKey)
        }
    }

    @discardableResult
    public func setBookmarked(dealId: Int, isBookmarked: Bool) -> Set<Int> {
        guard dealId > 0 else { return loadBookmarkedIds() }
        var ids = loadBookmarkedIds()
        if isBookmarked {
            ids.insert(dealId)
        } else {
            ids.remove(dealId)
        }
        saveBookmarkedIds(ids)

        let rawData = primaryDefaults.data(forKey: Self.cachedDealsKey)
            ?? (mirrorToStandard ? fallbackDefaults.data(forKey: Self.cachedDealsKey) : nil)
        if let data = rawData,
           let decoded = try? JSONDecoder().decode([WatchDealModel].self, from: data) {
            let updated = decoded.map { deal -> WatchDealModel in
                var copy = deal
                copy.isBookmarked = ids.contains(deal.id)
                return copy
            }
            if let encoded = try? JSONEncoder().encode(updated) {
                primaryDefaults.set(encoded, forKey: Self.cachedDealsKey)
                if mirrorToStandard {
                    if primaryDefaults !== fallbackDefaults {
                        fallbackDefaults.set(encoded, forKey: Self.cachedDealsKey)
                    }
                    writeSharedCacheFile(
                        deals: updated,
                        bookmarkedIds: ids,
                        timestamp: Date().timeIntervalSince1970
                    )
                }
            }
        } else if mirrorToStandard, let existing = readSharedCacheFile() {
            let updated = existing.deals.map { deal -> WatchDealModel in
                var copy = deal
                copy.isBookmarked = ids.contains(deal.id)
                return copy
            }
            writeSharedCacheFile(
                deals: updated,
                bookmarkedIds: ids,
                timestamp: Date().timeIntervalSince1970
            )
        }
        return ids
    }

    public func mergeDealsWithBookmarks(
        _ deals: [WatchDealModel],
        extraBookmarkedIds: Set<Int> = []
    ) -> [WatchDealModel] {
        var ids = loadBookmarkedIds().union(extraBookmarkedIds.filter { $0 > 0 })
        for deal in deals where deal.isBookmarked {
            ids.insert(deal.id)
        }
        if !extraBookmarkedIds.isEmpty {
            saveBookmarkedIds(ids)
        }
        return deals.map { deal in
            var copy = deal
            copy.isBookmarked = ids.contains(deal.id)
            return copy
        }
    }

    public func topActiveDeal(now: Date = Date()) -> WatchDealModel? {
        let deals = loadDeals()
        guard !deals.isEmpty else { return nil }
        let activeDeals = deals.filter { !$0.isExpired(now: now) }
        let pool = activeDeals.isEmpty ? deals : activeDeals
        return pool.sorted { lhs, rhs in
            if lhs.isHot != rhs.isHot {
                return lhs.isHot && !rhs.isHot
            }
            let lRating = lhs.rating ?? 0.0
            let rRating = rhs.rating ?? 0.0
            if abs(lRating - rRating) > 0.001 {
                return lRating > rRating
            }
            return (lhs.students ?? 0) > (rhs.students ?? 0)
        }.first
    }

    public func activeDealCount(now: Date = Date()) -> Int {
        return loadDeals().filter { !$0.isExpired(now: now) }.count
    }

    public func clearAll() {
        primaryDefaults.removeObject(forKey: Self.cachedDealsKey)
        primaryDefaults.removeObject(forKey: Self.bookmarkedIdsKey)
        primaryDefaults.removeObject(forKey: Self.lastCacheTimestampKey)
        if mirrorToStandard {
            if primaryDefaults !== fallbackDefaults {
                fallbackDefaults.removeObject(forKey: Self.cachedDealsKey)
                fallbackDefaults.removeObject(forKey: Self.bookmarkedIdsKey)
                fallbackDefaults.removeObject(forKey: Self.lastCacheTimestampKey)
            }
            try? FileManager.default.removeItem(atPath: Self.sharedCacheFilePath)
        }
    }

    private func writeSharedCacheFile(
        deals: [WatchDealModel],
        bookmarkedIds: Set<Int>,
        timestamp: TimeInterval
    ) {
        let payload = SharedWatchCachePayload(
            deals: deals,
            bookmarkedIds: bookmarkedIds.filter { $0 > 0 }.sorted(),
            timestamp: timestamp
        )
        guard let data = try? JSONEncoder().encode(payload) else { return }
        try? data.write(to: URL(fileURLWithPath: Self.sharedCacheFilePath), options: .atomic)
    }

    private func readSharedCacheFile() -> SharedWatchCachePayload? {
        let url = URL(fileURLWithPath: Self.sharedCacheFilePath)
        guard let data = try? Data(contentsOf: url) else { return nil }
        return try? JSONDecoder().decode(SharedWatchCachePayload.self, from: data)
    }
}
