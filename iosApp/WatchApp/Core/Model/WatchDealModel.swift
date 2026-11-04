import Foundation
import SwiftUI
import CourseDealsWatch

/// `WatchDealModel` is imported directly from the shared Kotlin Multiplatform `:domain:coupons`
/// framework (`CourseDealsWatch.WatchDealDto`), eliminating model, JSON parser, and formatter duplication.
public typealias WatchDealModel = WatchDealDto

extension WatchDealDto: @retroactive Identifiable {
    public var courseId: Int { Int(id) }

    public var deepLinkUrl: URL {
        URL(string: deepLinkUrlString) ?? URL(string: "coursedeals://courses/\(id)")!
    }

    public func isExpired(now: Date = Date()) -> Bool {
        isExpiredAt(nowEpochMillis: Int64(now.timeIntervalSince1970 * 1000))
    }

    public func formattedTimeLeft(now: Date = Date()) -> String {
        formattedTimeLeftAt(nowEpochMillis: Int64(now.timeIntervalSince1970 * 1000))
    }
}

public enum WatchHotDealsEnvelope {
    public static func decodeDeals(from data: Data, limit: Int32 = 20) -> [WatchDealModel] {
        guard let jsonString = String(data: data, encoding: .utf8) else { return [] }
        return WatchDealJsonCodec.shared.decodeDealsFromJson(
            jsonString: jsonString,
            limit: limit
        )
    }

    public static func decodeDeals(from jsonObject: Any) -> [WatchDealModel] {
        guard JSONSerialization.isValidJSONObject(jsonObject),
              let data = try? JSONSerialization.data(withJSONObject: jsonObject) else { return [] }
        return decodeDeals(from: data)
    }

    public static func encodeDeals(_ deals: [WatchDealModel]) -> Data? {
        WatchDealJsonCodec.shared.encodeDealsToJson(deals: deals).data(using: .utf8)
    }
}
