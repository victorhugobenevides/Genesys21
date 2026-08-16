# 🧬 Genesys21

[![CI Status](https://github.com/victorhugobenevides/Genesys21/actions/workflows/ci.yml/badge.svg)](https://github.com/victorhugobenevides/Genesys21/actions/workflows/ci.yml)
[![CodeQL](https://github.com/victorhugobenevides/Genesys21/security/analysis?query=codeql)](https://github.com/victorhugobenevides/Genesys21/security/code-scanning)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.7.3-blue)](https://github.com/JetBrains/compose-multiplatform)
[![Ktor](https://img.shields.io/badge/Ktor-3.0.3-orange?logo=ktor)](https://ktor.io)

**Genesys21** is a high-performance, White-Label engine built with **Kotlin Multiplatform**. It allows merchants to create, customize, and publish professional sales vitrines and landing pages across Android, iOS, and Web using a single shared codebase.

---

## ✨ Key Features

- 🛠️ **Real-time WhiteLabel Editor**: Live preview engine to customize themes, components, and products.
- 📊 **Advanced Merchant Cockpit**: Built-in Analytics Dashboard with revenue charts (Canvas), top products, and booking management.
- 💳 **Stripe Dynamic Checkout**: Embedded **Payment Element** for a seamless, on-site purchase experience without redirects.
- 🎨 **Dynamic Theme Engine**: Advanced styling with Glassmorphism, custom palettes, and curated typography.
- 📱 **Adaptive UI**: High-fidelity experiences optimized for Mobile (393dp), Tablet (600dp), and Desktop (1200dp).
- 📅 **Booking System**: Professional scheduling engine with Google Calendar integration for services.
- 🔒 **Security & Privacy**: Rate limiting, HSTS/CSP headers, and full LGPD compliance (Account deletion/Audit logs).

---

## 🛠️ Tech Stack

- **Core**: [Kotlin Multiplatform (KMP)](https://kotlinlang.org/docs/multiplatform.html)
- **UI**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) (Shared UI for Android, iOS, Web)
- **Dependency Injection**: [Koin](https://insert-koin.io/)
- **Networking**: [Ktor Client](https://ktor.io/docs/client.html)
- **Database**: Exposed (Server) + SQLite
- **Backend**: [Ktor Server](https://ktor.io/docs/server-overview.html)
- **Payments**: [Stripe SDK 33.3.0+](https://stripe.com/docs/api)
- **Testing**: [Paparazzi](https://github.com/cashapp/paparazzi) (Visual Snapshots)
- **Infrastructure**: Docker, Nginx, GitHub Actions, CircleCI

---

## 📂 Project Structure

```bash
├── composeApp/      # Shared UI code (Compose Multiplatform)
│   ├── commonMain   # Main shared UI and navigation logic
│   ├── androidMain  # Android-specific UI/Lifecycle
│   └── wasmJsMain   # Web-specific (Wasm) logic
├── iosApp/          # iOS SwiftUI wrapper and entry point
├── shared/          # Shared Business Logic (Domain, Data, Repositories)
├── server/          # Ktor Backend (REST API, Database, Analytics)
└── screenshot-tests/ # Visual regression tests (JVM)
```

---

## 🚀 Getting Started

### Prerequisites
- **JDK 21**
- **Android Studio Ladybug+** (or IntelliJ IDEA)
- **Xcode 15+** (for iOS development)
- **Docker** (optional, for backend deployment)

### 1. Firebase Configuration (Mandatory)
Genesys21 requires Firebase to function. Add your `google-services.json` and `GoogleService-Info.plist` files:

- **Android:** `composeApp/google-services.json`
- **iOS:** `iosApp/iosApp/GoogleService-Info.plist`
- **Server:** `server/firebase-adminsdk.json`

### 2. Stripe Integration
The platform uses the **Payment Element**. Ensure your Merchant accounts are configured in the Stripe Dashboard and your `STRIPE_SECRET_KEY` is set in the environment variables.

---

## 📱 Development Guide

### Run Android App
```shell
./gradlew :composeApp:installDebug
```

### Run Server (Ktor)
```shell
./gradlew :server:run
```

### Run Web (Wasm/JS)
```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

### Run iOS
Open `iosApp/iosApp.xcworkspace` in Xcode and run the `iosApp` scheme.

---

## 📄 License

Copyright © 2024 It Benevides. All rights reserved.
Developed by [Victor Hugo Benevides](https://github.com/victorhugobenevides).
