import SwiftUI

@main
struct iOSApp: App {
    @StateObject private var watchConnectivity = PhoneWatchConnectivityManager.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
