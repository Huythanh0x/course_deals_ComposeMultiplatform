import Foundation
import SwiftUI

public enum WatchCoursePlatform: String, Codable, CaseIterable {
    case udemy = "Udemy"
    case coursera = "Coursera"
    case edx = "edX"

    public var displayName: String { rawValue }

    public var tagColor: Color {
        switch self {
        case .udemy: return Color(red: 0.63, green: 0.23, blue: 0.82)
        case .coursera: return Color(red: 0.0, green: 0.34, blue: 0.72)
        case .edx: return Color(red: 0.85, green: 0.15, blue: 0.15)
        }
    }

    public static func infer(raw: String? = nil, previewImage: String? = nil, title: String? = nil) -> WatchCoursePlatform {
        let combined = "\(raw ?? "") \(previewImage ?? "") \(title ?? "")".lowercased()
        if combined.contains("coursera") { return .coursera }
        if combined.contains("edx") { return .edx }
        return .udemy
    }
}

/// Compact `Codable` domain model mirroring Kotlin `@Serializable WatchDealDto` and backend `/hot-deals` JSON.
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

    public var discountBadgeText: String { "100% OFF" }
    public var deepLinkUrl: URL { URL(string: "coursedeals://courses/\(id)")! }

    public init(
        id: Int = 501,
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

    private enum CodingKeys: String, CodingKey {
        case id, courseId, title, heading, author, category, couponCode, couponUrl, targetUrl, previewImage
        case expiredTime, expiresAtEpochSeconds, rating, reviews, usesRemaining, usesLeft, platform, isBookmarked, students, isHot
    }

    public init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        let titleVal = try c.decode(String.self, forKey: .title)
        let directId = try? c.decodeIfPresent(Int.self, forKey: .id)
        let courseIdVal = try? c.decodeIfPresent(Int.self, forKey: .courseId)
        let strId = (try? c.decodeIfPresent(String.self, forKey: .id)) ?? ""
        let idVal = directId ?? courseIdVal ?? Int(strId) ?? 501

        let preview = try? c.decodeIfPresent(String.self, forKey: .previewImage)
        let reviewsVal = (try? c.decodeIfPresent(Int.self, forKey: .reviews)) ?? 240
        let expInt = try? c.decodeIfPresent(Int64.self, forKey: .expiredTime)
        let expDbl = (try? c.decodeIfPresent(Double.self, forKey: .expiredTime))
            ?? (try? c.decodeIfPresent(Double.self, forKey: .expiresAtEpochSeconds))
        let expVal = expInt ?? (expDbl != nil ? Int64(expDbl!) : nil)
        let urlVal = (try? c.decodeIfPresent(String.self, forKey: .couponUrl))
            ?? (try? c.decodeIfPresent(String.self, forKey: .targetUrl))
            ?? preview ?? "https://coursedeals.com"
        let usesVal = (try? c.decodeIfPresent(Int.self, forKey: .usesRemaining))
            ?? (try? c.decodeIfPresent(Int.self, forKey: .usesLeft))
            ?? max(5, reviewsVal / 20)

        self.init(
            id: idVal,
            title: titleVal,
            heading: try? c.decodeIfPresent(String.self, forKey: .heading),
            author: (try? c.decodeIfPresent(String.self, forKey: .author)) ?? "Udemy Instructor",
            category: (try? c.decodeIfPresent(String.self, forKey: .category)) ?? "Development",
            couponCode: (try? c.decodeIfPresent(String.self, forKey: .couponCode)) ?? "HOT100",
            couponUrl: urlVal,
            expiredTime: expVal,
            rating: (try? c.decodeIfPresent(Double.self, forKey: .rating)) ?? 4.8,
            reviews: reviewsVal,
            usesRemaining: usesVal,
            platform: WatchCoursePlatform.infer(
                raw: try? c.decodeIfPresent(String.self, forKey: .platform),
                previewImage: preview,
                title: titleVal
            ),
            isBookmarked: (try? c.decodeIfPresent(Bool.self, forKey: .isBookmarked)) ?? false,
            students: (try? c.decodeIfPresent(Int.self, forKey: .students)) ?? 0,
            isHot: (try? c.decodeIfPresent(Bool.self, forKey: .isHot)) ?? true
        )
    }

    public func encode(to encoder: Encoder) throws {
        var c = encoder.container(keyedBy: CodingKeys.self)
        try c.encode(id, forKey: .id)
        try c.encode(title, forKey: .title)
        try c.encodeIfPresent(heading, forKey: .heading)
        try c.encodeIfPresent(author, forKey: .author)
        try c.encodeIfPresent(category, forKey: .category)
        try c.encodeIfPresent(couponCode, forKey: .couponCode)
        try c.encodeIfPresent(couponUrl, forKey: .couponUrl)
        try c.encodeIfPresent(expiredTime, forKey: .expiredTime)
        try c.encodeIfPresent(rating, forKey: .rating)
        try c.encodeIfPresent(reviews, forKey: .reviews)
        try c.encodeIfPresent(usesRemaining, forKey: .usesRemaining)
        try c.encode(platform.rawValue, forKey: .platform)
        try c.encode(isBookmarked, forKey: .isBookmarked)
        try c.encodeIfPresent(students, forKey: .students)
        try c.encode(isHot, forKey: .isHot)
    }

    public var formattedRating: String {
        if let rating = rating, rating > 0 { return String(format: "★ %.1f", rating) }
        return "★ 4.8"
    }

    public var formattedStudents: String {
        guard let students = students, students > 0 else { return "" }
        if students >= 1_000_000 { return String(format: "%.1fM students", Double(students) / 1_000_000.0) }
        if students >= 1_000 { return "\(Int((Double(students) / 1_000.0).rounded()))k students" }
        return "\(students) students"
    }

    public func isExpired(now: Date = Date()) -> Bool {
        guard let expiredTime = expiredTime, expiredTime > 0 else { return false }
        let nowMillis = Int64(now.timeIntervalSince1970 * 1000)
        let expireMillis = expiredTime > 10_000_000_000 ? expiredTime : expiredTime * 1000
        return expireMillis <= nowMillis
    }

    public func formattedTimeLeft(now: Date = Date()) -> String {
        guard let expiredTime = expiredTime, expiredTime > 0 else { return "2h left" }
        let nowMillis = Int64(now.timeIntervalSince1970 * 1000)
        let expireMillis = expiredTime > 10_000_000_000 ? expiredTime : expiredTime * 1000
        let diffMillis = expireMillis - nowMillis
        if diffMillis <= 0 { return "Expired" }
        let totalMinutes = diffMillis / 60_000
        let hours = totalMinutes / 60
        let minutes = totalMinutes % 60
        if hours > 24 { return "\(hours / 24)d left" }
        if hours > 0 { return "\(hours)h \(minutes)m left" }
        return "\(max(1, minutes))m left"
    }
}

public struct WatchHotDealsEnvelope: Codable {
    public let courses: [WatchDealModel]?
    public let deals: [WatchDealModel]?

    public static func decodeDeals(from data: Data) -> [WatchDealModel] {
        let decoder = JSONDecoder()
        if let direct = try? decoder.decode([WatchDealModel].self, from: data) { return direct }
        if let env = try? decoder.decode(WatchHotDealsEnvelope.self, from: data) {
            return env.courses ?? env.deals ?? []
        }
        return []
    }

    public static func decodeDeals(from jsonObject: Any) -> [WatchDealModel] {
        guard JSONSerialization.isValidJSONObject(jsonObject),
              let data = try? JSONSerialization.data(withJSONObject: jsonObject) else { return [] }
        return decodeDeals(from: data)
    }
}
