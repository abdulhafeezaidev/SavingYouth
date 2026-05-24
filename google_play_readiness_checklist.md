# Google Play Store Distribution & Readiness Checklist

This document details the configuration requirements, permission justifications, and Data Safety declarations for distributing **Behavioral Firewall** on the Google Play Store or to internal testers.

---

## 🔑 1. Release Keystore & Build Signing Config

To deploy release builds safely, configure Android signing securely. Do **NOT** commit raw credentials to version control.

### Step 1: Generate Release Keystore
Run the following keytool command to generate a secure keystore:
```bash
keytool -genkey -v -keystore release.keystore -alias behavioral_firewall_alias -keyalg AES -keysize 256 -validity 10000
```

### Step 2: Configure Environment Variables
Define the following in your shell environment or local development machine `local.properties` (never commit keys to git):
```properties
RELEASE_KEYSTORE_PASSWORD=secure_keystore_password_here
RELEASE_KEY_ALIAS=behavioral_firewall_alias
RELEASE_KEY_PASSWORD=secure_key_password_here
```

### Step 3: Gradle Signing Configuration
In `app/build.gradle.kts`, reference the secure environment variables dynamically:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("RELEASE_KEYSTORE_PATH") ?: "release.keystore")
            storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

---

## 🔒 2. Proguard / R8 Shrinking & Code Obfuscation

Our `proguard-rules.pro` was enabled and configured in Day 12. Complete the following sanity checks before releasing:
- [x] Run `./gradlew assembleRelease` to verify that R8 does not throw dependency resolution warnings.
- [x] Verify Hilt generated components are preserved (`-keep class * implements dagger.hilt.internal.GeneratedComponent`).
- [x] Verify Room database entities are preserved and not obfuscated (`-keepclassmembers class * extends androidx.room.RoomDatabase`).
- [x] Verify SQLCipher native bindings are preserved (`-keep class net.sqlcipher.** { *; }`).

---

## 📝 3. Android High-Risk Permissions Justification

"Behavioral Firewall" utilizes three high-privilege Android platform features. Google Play Console requires clear, transparent video demonstrations and strict declaration forms:

### A. Accessibility Service (`BIND_ACCESSIBILITY_SERVICE`)
- **Category**: Accessibility Services Policy
- **Justification Statement**: 
  > "Behavioral Firewall utilizes Android's Accessibility Service to detect rapid app switches, trigger overlays, and introduce healthy friction into high-risk behavioral patterns. This service operates strictly on-device, logs no user inputs, transmits no data externally, and is vital for real-time behavioral self-regulation support. No personal or sensitive user data is collected or stored."
- **Policy Compliance**: Ensure your app description explicitly states that the Accessibility Service is used for behavioral self-regulation and requires active, conscious user opt-in.

### B. VPN Service (`VpnService`)
- **Category**: VpnService Policy (Device-on-device traffic routing)
- **Justification Statement**: 
  > "The application utilizes VpnService to establish a local, on-device loopback tunnel. This routes UDP port 53 DNS requests through our local filters to identify and block restricted high-risk web domains (Safe Browsing). Traffic never leaves the device, no remote VPN servers are utilized, and no web activity is logged or transmitted. This local filter operates strictly offline to safeguard user privacy."
- **Policy Compliance**: VPN usage must align with the "Device Automation & Self-Regulation" category.

### C. System Alert Window (`SYSTEM_ALERT_WINDOW` / Draw Over Overlays)
- **Category**: Draw Over Other Apps Permission
- **Justification Statement**: 
  > "Required to display calming behavioral overlay screens and introducing intentional, healthy visual delays when high-risk trigger states are identified, helping users pause and reflect before proceeding. Overlays can be disabled at any time via Settings."

---

## 📊 4. Google Play Data Safety Declarations

Use the following mapping to complete the **Data Safety Questionnaire** inside Google Play Console:

| Question Section | App Declaration / Answer | Description / Technical Mapping |
| :--- | :--- | :--- |
| **Data Collection** | **No Personal Data Collected** | The app processes all behavioral risks, app usage, and screen states entirely local-to-device. |
| **Data in Transit** | **Encrypted in Transit** | Any optional developer-initiated sync data (e.g. encrypted backups, anonymous feedback) uses HTTPS TLS 1.3 encryption. |
| **Data Deletion** | **Yes (User-initiated)** | The app provides a prominent "Wipe Local Data" button in Settings that instantly wipes all local SQLite risk logs. |
| **Data Categories** | **App Activity & Diagnostics** | Only anonymous analytics events (onboarding success, streak milestones) and crash logs are transmitted (if opt-in). |
| **Third-Party Sharing**| **No Data Shared** | Absolutely zero user behavioral logs, package names, or websites visited are shared with third parties. |

---

## 🛡️ 5. Privacy Policy Core Structure

Your hosted Privacy Policy URL must prominently declare:
1. **Local Processing First**: Explicit statement that all accessibility inputs, VPN domain evaluations, and trigger analytics occur strictly on the user's local device inside SQLCipher encrypted databases.
2. **Zero Sensitive Data Syncing**: Statement declaring that raw web URLs, package identifiers, or search inputs are never tracked, synced, or analyzed.
3. **Pluggable & Anonymous Crash Reports**: Statement clarifying that diagnostic telemetry is entirely anonymous and automatically redacts any unintentional PII (such as emails or URL segments) using on-device sanitization.
4. **Consent-Based Feedback**: In-app feedback forms are processed strictly anonymously with zero account linkages.
