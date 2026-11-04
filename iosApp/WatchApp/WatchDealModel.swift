import Foundation
import SwiftUI

/// SwiftUI & formatting extensions on auto-generated `WatchDealModel` and `WatchCoursePlatform`
/// (`iosApp/WatchApp/Generated/WatchDealDto.generated.swift`).
extension WatchDealModel {
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

extension WatchCoursePlatform {
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
