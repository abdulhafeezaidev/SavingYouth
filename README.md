# Behavioral Firewall (SavingYouth) — High-Scale Android Digital Safety Engine

A privacy-preserving, local-first Android behavioral firewall designed to protect youth from harmful digital patterns, high-risk content, and addictive screen loops through on-device heuristics, encrypted local storage, and batched synchronization.

---

## Architecture Overview

```text
+-----------------------------------------------------------------------------------+
|                        On-Device Behavioral Detection (Kotlin)                    |
|                                                                                   |
|  - Real-time foreground accessibility & screen event monitoring                   |
|  - On-device heuristic risk scoring (Zero cloud latency, instant interception)   |
|  - Permission & Device Admin manager (`PermissionManager.kt`)                     |
+-----------------------------------------+-----------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                     Local-First Encrypted Room Database (SQLCipher)               |
|                                                                                   |
|  - RiskEventEntity             - AccountabilityPartnerEntity                      |
|  - AppUsageEntity              - BlockedAttemptEntity                             |
|  - AnalyticsSummaryEntity      - Hour-Level Telemetry Aggregation                 |
+-----------------------------------------+-----------------------------------------+
                                          |
                                          | Wi-Fi & Charging Triggered
                                          v
+-----------------------------------------------------------------------------------+
|                        Scalable Backend Synchronization Pipeline                  |
|                                                                                   |
|  - Write Buffering: 5-minute memory buffering cuts database IOPS                  |
|  - Compressed Bulk Bundles: Gzip JSON arrays reduce payload size by up to 85%     |
|  - High-Scale Appwrite Cluster: Scales efficiently from 10K to 1M+ active users   |
+-----------------------------------------------------------------------------------+
```

---

## Core Engineering Capabilities

1. **Local-First & Zero-Latency Interception**:
   - All risk score calculations and application blocking occur **100% on-device** via native Android services. No network roundtrip is required to protect the user.
   - Operates with 100% feature fidelity when offline. Telemetry events and audit logs are queued locally.

2. **Battery & Network Optimization**:
   - **Write Buffering**: Application usage logs are buffered in memory and flushed in batches every 5 minutes (or 50 logs), eliminating storage thrashing.
   - **Hourly Aggregation**: Granular risk events are condensed on-device into compact hourly averages before sync.
   - **Gzip Bundled Sync**: Sync triggers strictly during Wi-Fi and charging windows, uploading compressed bundles to minimize network radio wakeups.

3. **High-Scale System Architecture**:
   - Engineered with modular scalability blueprints (`scaling_architecture_design.md`) designed to transition smoothly from 10K to 1M+ active users without cloud cost explosion or server degradation.

4. **Production Architecture**:
   - Modern Android architecture following Clean Architecture principles: `core`, `data`, `di`, `domain`, `presentation`, and `services`.
   - Dependency injection powered by **Dagger Hilt**, persistence powered by **Room / SQLite**, and asynchronous pipelines powered by **Kotlin Coroutines & Flow**.

---

## Technology Stack

- **Platform**: Android Native (API 26+)
- **Language**: Kotlin 1.9+
- **Architecture**: Clean Architecture + MVVM + Repository Pattern
- **Dependency Injection**: Dagger Hilt
- **Local Persistence**: Room Database, SQLite (SQLCipher encryption ready)
- **Asynchronous / Reactive**: Kotlin Coroutines, StateFlow / SharedFlow
- **Cloud Backend Sync**: Appwrite Cloud / Self-hosted Cluster integration
- **Documentation**: Includes complete specifications for scaling architecture, Google Play store readiness, and continuous improvement pipelines.

---

## Project Structure

```text
SavingYouth/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/behavioralfirewall/
│   │   ├── core/
│   │   │   ├── constants/             # AppConstants, policy thresholds
│   │   │   ├── security/              # Cryptographic utilities & device admin
│   │   │   └── utils/                 # PermissionManager, hardware sensors
│   │   ├── data/
│   │   │   ├── local/                 # Room database (AppDatabase, DAOs, entities)
│   │   │   │   ├── dao/               # BehavioralDao
│   │   │   │   └── entities/          # RiskEvent, AppUsage, BlockedAttempt
│   │   │   ├── remote/                # Appwrite sync client & API mappers
│   │   │   └── repository/            # BehavioralRepository implementation
│   │   ├── di/                        # Hilt dependency injection modules
│   │   ├── domain/                    # Use cases, models, repository contracts
│   │   ├── presentation/              # UI screens, ViewModels, and state holders
│   │   └── services/                  # Foreground monitoring & accessibility services
│   └── res/                           # Android layouts, drawables, strings
├── scaling_architecture_design.md     # 10K-to-1M user scaling blueprint
├── google_play_readiness_checklist.md # Production compliance audit
└── continuous_improvement_pipeline.md # Automated CI/CD & release pipeline
```

---

## Setup & Build

### Prerequisites
- Android Studio Hedgehog (2023.1.1+) or newer
- JDK 17
- Android SDK 34

### Building from Command Line
```bash
# Clone the repository
git clone https://github.com/abdulhafeezaidev/SavingYouth.git
cd SavingYouth

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```
