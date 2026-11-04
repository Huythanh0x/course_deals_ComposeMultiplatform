import SwiftUI
#if canImport(WidgetKit)
import WidgetKit
#endif

#if canImport(WidgetKit) && (os(watchOS) || os(iOS))
/// Entry point bundle combining Watch Face Complications and watchOS 10+ Smart Stack widget.
#if WIDGET_EXTENSION
@main
#endif
public struct WatchWidgetBundle: WidgetBundle {
    public init() {}

    public var body: some Widget {
        HotDealsComplicationWidget()
        SmartStackHotDealWidget()
    }
}
#endif
