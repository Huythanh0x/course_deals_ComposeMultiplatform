import Foundation
import SwiftUI

/// Domain model for watchOS companion app mirroring KMP `Coupon` domain entity.
public struct WatchDealModel: Identifiable, Hashable, Codable {
    public let id: Int
    public let title: String
    public let heading: String?
    public let author: String?
    public let category: String?
    public let couponCode: String?
    public let couponUrl: String?
    public let expiredTime: Int64?
    public let rating: Double?
    public let reviews: Int?
    public let usesRemaining: Int?
    public let platform: WatchCoursePlatform
    public var isBookmarked: Bool
    public let students: Int?
    public let isHot: Bool

    public init(
        id: Int,
        title: String,
        heading: String? = nil,
        author: String? = nil,
        category: String? = nil,
        couponCode: String? = nil,
        couponUrl: String? = nil,
        expiredTime: Int64? = nil,
        rating: Double? = nil,
        reviews: Int? = nil,
        usesRemaining: Int? = nil,
        platform: WatchCoursePlatform = .udemy,
        isBookmarked: Bool = false,
        students: Int? = nil,
        isHot: Bool = true
    ) {
        self.id = id
        self.title = title
        self.heading = heading
        self.author = author
        self.category = category
        self.couponCode = couponCode
        self.couponUrl = couponUrl
        self.expiredTime = expiredTime
        self.rating = rating
        self.reviews = reviews
        self.usesRemaining = usesRemaining
        self.platform = platform
        self.isBookmarked = isBookmarked
        self.students = students
        self.isHot = isHot
    }

    public var discountBadgeText: String {
        return "100% OFF"
    }

    public var deepLinkUrl: URL {
        return URL(string: "coursedeals://courses/\(id)") ?? URL(string: "coursedeals://courses/501")!
    }

    public var formattedRating: String {
        if let rating = rating, rating > 0 {
            return String(format: "★ %.1f", rating)
        }
        return "★ 4.8"
    }

    public var formattedStudents: String {
        guard let students = students, students > 0 else { return "" }
        if students >= 1_000_000 {
            let mVal = Double(students) / 1_000_000.0
            return String(format: "%.1fM students", mVal)
        } else if students >= 1_000 {
            let kVal = Int((Double(students) / 1_000.0).rounded())
            return "\(kVal)k students"
        } else {
            return "\(students) students"
        }
    }

    public func isExpired(now: Date = Date()) -> Bool {
        guard let expiredTime = expiredTime, expiredTime > 0 else { return false }
        let nowMillis = Int64(now.timeIntervalSince1970 * 1000)
        let expireMillis = expiredTime > 10_000_000_000 ? expiredTime : expiredTime * 1000
        return expireMillis <= nowMillis
    }

    public func formattedTimeLeft(now: Date = Date()) -> String {
        guard let expiredTime = expiredTime, expiredTime > 0 else {
            return "2h left"
        }
        let nowMillis = Int64(now.timeIntervalSince1970 * 1000)
        let expireMillis = expiredTime > 10_000_000_000 ? expiredTime : expiredTime * 1000
        let diffMillis = expireMillis - nowMillis

        if diffMillis <= 0 {
            return "Expired"
        }
        let totalMinutes = diffMillis / 60_000
        let hours = totalMinutes / 60
        let minutes = totalMinutes % 60

        if hours > 24 {
            let days = hours / 24
            return "\(days)d left"
        } else if hours > 0 {
            return "\(hours)h \(minutes)m left"
        } else {
            return "\(max(1, minutes))m left"
        }
    }
}

public enum WatchCoursePlatform: String, Codable, CaseIterable {
    case udemy = "Udemy"
    case coursera = "Coursera"
    case edx = "edX"

    public var displayName: String { rawValue }

    public var tagColor: Color {
        switch self {
        case .udemy:
            return Color(red: 0.63, green: 0.23, blue: 0.82)
        case .coursera:
            return Color(red: 0.0, green: 0.34, blue: 0.72)
        case .edx:
            return Color(red: 0.85, green: 0.15, blue: 0.15)
        }
    }
}

/// Persistent offline cache store for Apple Watch deals and bookmarked course IDs.
/// Uses App Group `group.com.thanh0x.coursedeals` with automatic fallback/mirroring to `.standard`
/// so both the main WatchApp and WidgetKit Complication/Smart Stack extensions read consistent state.
public final class WatchDealCacheStore {
    public static let shared = WatchDealCacheStore()

    public static let appGroupSuiteName = "group.com.thanh0x.coursedeals"
    public static let cachedDealsKey = "cached_watch_deals_v1"
    public static let bookmarkedIdsKey = "bookmarked_watch_deal_ids_v1"
    public static let lastCacheTimestampKey = "last_cache_timestamp_v1"

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

        if mirrorToStandard && primaryDefaults !== fallbackDefaults {
            fallbackDefaults.set(encoded, forKey: Self.cachedDealsKey)
            fallbackDefaults.set(now, forKey: Self.lastCacheTimestampKey)
        }
    }

    public func loadDeals() -> [WatchDealModel] {
        let rawData = primaryDefaults.data(forKey: Self.cachedDealsKey)
            ?? (mirrorToStandard ? fallbackDefaults.data(forKey: Self.cachedDealsKey) : nil)

        guard let data = rawData else {
            return []
        }

        guard let decoded = try? JSONDecoder().decode([WatchDealModel].self, from: data) else {
            return []
        }

        let bookmarked = loadBookmarkedIds()
        return decoded.map { deal in
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
            if let array = fallbackDefaults.array(forKey: Self.bookmarkedIdsKey) as? [Int] {
                return Set(array.filter { $0 > 0 })
            }
            if let nsNumbers = fallbackDefaults.array(forKey: Self.bookmarkedIdsKey) as? [NSNumber] {
                return Set(nsNumbers.map { $0.intValue }.filter { $0 > 0 })
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

        // Update cached deals payload if present
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
                if mirrorToStandard && primaryDefaults !== fallbackDefaults {
                    fallbackDefaults.set(encoded, forKey: Self.cachedDealsKey)
                }
            }
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
        if mirrorToStandard && primaryDefaults !== fallbackDefaults {
            fallbackDefaults.removeObject(forKey: Self.cachedDealsKey)
            fallbackDefaults.removeObject(forKey: Self.bookmarkedIdsKey)
            fallbackDefaults.removeObject(forKey: Self.lastCacheTimestampKey)
        }
    }
}

