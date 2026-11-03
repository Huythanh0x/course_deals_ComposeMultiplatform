import SwiftUI

@main
struct iOSApp: App {
    @StateObject private var watchConnectivity = PhoneWatchConnectivityManager.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onContinueUserActivity(PhoneWatchConnectivityManager.handoffActivityType) { activity in
                    watchConnectivity.handleUserActivity(activity)
                }
                .onContinueUserActivity(PhoneWatchConnectivityManager.legacyHandoffActivityType) { activity in
                    watchConnectivity.handleUserActivity(activity)
                }
        }
    }
}
