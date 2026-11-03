import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @ObservedObject private var watchConnectivity = PhoneWatchConnectivityManager.shared

    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
            .onOpenURL { url in
                MainViewControllerKt.handleDeepLink(url: url.absoluteString)
            }
            .onContinueUserActivity(PhoneWatchConnectivityManager.handoffActivityType) { activity in
                watchConnectivity.handleUserActivity(activity)
            }
            .onContinueUserActivity(PhoneWatchConnectivityManager.legacyHandoffActivityType) { activity in
                watchConnectivity.handleUserActivity(activity)
            }
            .onChange(of: watchConnectivity.pendingDeepLinkUrl) { newUrl in
                if let newUrl = newUrl, !newUrl.isEmpty {
                    MainViewControllerKt.handleDeepLink(url: newUrl)
                }
            }
    }
}
