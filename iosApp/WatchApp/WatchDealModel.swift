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

extension WatchDealModel {
    /// Sample flash deals for watchOS glanceable UI demonstration.
    public static var sampleDeals: [WatchDealModel] {
        let now = Date().timeIntervalSince1970 * 1000
        let oneHourMillis: Double = 3600 * 1000

        return [
            WatchDealModel(
                id: 101,
                title: "The Complete Kotlin & KMP Masterclass 2026",
                heading: "Build multiplatform mobile & desktop apps with Compose",
                author: "Alex Rivers",
                category: "Development",
                couponCode: "KMP100FREE",
                couponUrl: "https://www.udemy.com/course/kotlin-multiplatform-masterclass/?couponCode=KMP100FREE",
                expiredTime: Int64(now + (0.7 * oneHourMillis)),
                rating: 4.9,
                reviews: 1420,
                usesRemaining: 14,
                platform: .udemy,
                isBookmarked: false,
                students: 45200,
                isHot: true
            ),
            WatchDealModel(
                id: 102,
                title: "Machine Learning with Python & PyTorch",
                heading: "Hands-on deep neural network training from scratch",
                author: "Dr. Elena Rostova",
                category: "Data Science",
                couponCode: "MLDEAL100",
                couponUrl: "https://www.coursera.org/learn/machine-learning-pytorch?couponCode=MLDEAL100",
                expiredTime: Int64(now + (1.25 * oneHourMillis)),
                rating: 4.8,
                reviews: 890,
                usesRemaining: 8,
                platform: .coursera,
                isBookmarked: true,
                students: 31500,
                isHot: true
            ),
            WatchDealModel(
                id: 103,
                title: "iOS 18 & SwiftUI Architecture Deep Dive",
                heading: "Modern SwiftUI state management, navigation, and testing",
                author: "Marcus Chen",
                category: "Mobile",
                couponCode: "SWIFTUI2026",
                couponUrl: "https://www.udemy.com/course/swiftui-mastery-ios18/?couponCode=SWIFTUI2026",
                expiredTime: Int64(now + (2.5 * oneHourMillis)),
                rating: 4.9,
                reviews: 2150,
                usesRemaining: 23,
                platform: .udemy,
                isBookmarked: false,
                students: 28900,
                isHot: true
            ),
            WatchDealModel(
                id: 104,
                title: "Distributed Systems & Cloud Architecture",
                heading: "Scalable backend patterns with Kafka, Redis, and Go",
                author: "Sarah Jenkins",
                category: "Cloud",
                couponCode: "SCALENOW",
                couponUrl: "https://www.coursera.org/learn/cloud-distributed-systems?couponCode=SCALENOW",
                expiredTime: Int64(now + (4.0 * oneHourMillis)),
                rating: 4.7,
                reviews: 640,
                usesRemaining: 5,
                platform: .coursera,
                isBookmarked: false,
                students: 19400,
                isHot: true
            ),
            WatchDealModel(
                id: 105,
                title: "Clean Architecture in Android & KMP",
                heading: "Domain-driven layered design with MVI & Coroutines",
                author: "David Thorne",
                category: "Architecture",
                couponCode: "CLEANKMP100",
                couponUrl: "https://www.udemy.com/course/clean-architecture-android-kmp/?couponCode=CLEANKMP100",
                expiredTime: Int64(now + (6.0 * oneHourMillis)),
                rating: 4.9,
                reviews: 1820,
                usesRemaining: 31,
                platform: .udemy,
                isBookmarked: false,
                students: 16800,
                isHot: true
            )
        ]
    }
}
