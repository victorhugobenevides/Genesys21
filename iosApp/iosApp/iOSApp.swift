import SwiftUI
import FirebaseCore
import ComposeApp

@main
struct iOSApp: App {
    
    init() {
        FirebaseApp.configure()
        // O Koin agora é acessível porque exportamos o módulo shared no Gradle
        Koin_iosKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
