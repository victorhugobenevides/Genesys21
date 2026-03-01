import SwiftUI
import FirebaseCore
import ComposeApp

@main
struct iOSApp: App {
    
    init() {
        FirebaseApp.configure()
        // Koin precisa ser inicializado antes de qualquer chamada a ele
        Koin_iosKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    // Chama a função Kotlin para armazenar a URL
                    Koin_iosKt.setInitialDeepLink(url: url.absoluteString)
                    
                    // A navegação será tratada no lado do Kotlin quando a App for recarregada
                    // Para uma atualização em tempo real, seria necessário um mecanismo
                    // de notificação/callback do Swift para o Kotlin.
                }
        }
    }
}
