# Especificação: Dívida Técnica - Analytics iOS Nativo

Este documento registra o estado atual e as ações necessárias para restaurar a integração nativa do Firebase Analytics no iOS, que foi temporariamente desativada para permitir o build do projeto.

## Estado Atual
A implementação em [Analytics.ios.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/iosMain/kotlin/com/itbenevides/genesys21/util/Analytics.ios.kt) está como **No-op**. Os eventos são apenas impressos no log do console (`println`).

## Problema Encontrado
Durante a execução da task `:shared:cinteropFirebaseAnalyticsIos...`, o compilador Kotlin Native/Clang retornou o seguinte erro:
> `error: module '_c_standard_library_obsolete' requires feature 'found_incompatible_headers__check_search_paths'`

Este erro está relacionado a um conflito entre os headers do SDK do Xcode (v26.2 no ambiente atual) e o mecanismo de interoperação do Kotlin.

## Requisitos para Restauração

### 1. Configuração do Gradle
O plugin CocoaPods e o bloco de configuração devem ser reativados no arquivo `shared/build.gradle.kts`:

```kotlin
plugins {
    // ...
    kotlin("native.cocoapods")
}

kotlin {
    // ...
    cocoapods {
        summary = "Shared module for Genesys21"
        homepage = "https://github.com/itbenevides/genesys21"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        pod("FirebaseAnalytics")
        framework {
            baseName = "shared"
            isStatic = true
        }
    }
}
```

### 2. Implementação do Código
Descomentar o código em `Analytics.ios.kt` e importar `cocoapods.FirebaseAnalytics.FIRAnalytics`.

### 3. Ambiente de Build
- Verificar se o Xcode está atualizado e se o SDK path está correto (`xcode-select`).
- Tentar limpar o cache do Kotlin Native: `rm -rf ~/.konan`.
- Considerar o uso de uma versão específica do Firebase via Pod ou migrar para a biblioteca [GitLive Firebase Kotlin SDK](https://github.com/GitLiveApp/firebase-kotlin-sdk) de forma completa (evitando cinterop manual se possível).

---
> [!CAUTION]
> Não tente reativar o código sem antes resolver o erro de `modulemap` no ambiente, pois isso impedirá qualquer build de iOS (incluindo metadados no common).
