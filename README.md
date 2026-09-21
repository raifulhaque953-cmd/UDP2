# PocketShark

**A native Android packet capture, protocol inspector, and network traffic analyzer built with Kotlin and Jetpack Compose.**

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.12.01-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Download APK](https://img.shields.io/badge/Download-APK%20(v1.0.0)-success?logo=android&logoColor=white)](https://github.com/Shahbaz-ali22/PocketShark/releases/download/v1.0.0/app-debug.apk)

---

## 📥 Download APK

Get the latest pre-built APK directly from GitHub Releases:

* **Direct Download:** [**app-debug.apk (v1.0.0)**](https://github.com/Shahbaz-ali22/PocketShark/releases/download/v1.0.0/app-debug.apk) *(~16.7 MB)*
* **Release Page:** [PocketShark v1.0.0 Release](https://github.com/Shahbaz-ali22/PocketShark/releases/tag/v1.0.0)

> **Compatibility:** Requires Android 8.0 (API level 26) or higher. No root access required.

---

## Overview

PocketShark is a lightweight, non-root network packet capture and traffic analysis tool designed for Android devices. By creating a local virtual TUN interface via Android's `VpnService` API, PocketShark intercepts outgoing and incoming IPv4 traffic on the device and decodes network protocols in real time using a pure-Kotlin packet parsing engine.

The user interface is built entirely with Jetpack Compose, featuring a dark glassmorphic design system, hardware-accelerated background blur, interactive traffic waveforms, and swipeable multi-tab navigation.

---

## Features

### 1. Non-Root Packet Capture
* **Local TUN Interface:** Intercepts device network packets using Android's `VpnService` without requiring root permissions.
* **Configurable Network Parameters:** Customizable MTU (default: `1500`), virtual subnet IP (default: `10.0.0.2`), and primary/secondary DNS servers (defaults: `8.8.8.8` / `8.8.4.4`).
* **Foreground Service:** Runs capture in an Android Foreground Service with a sticky status bar notification.
* **UDP Proxy & Forwarding:** Forwards outbound UDP datagrams through protected sockets and returns responses into the TUN interface to maintain DNS and UDP connectivity during active capture.

### 2. Protocol Parsing & Inspection
* **Pure-Kotlin Parser:** Decodes raw IP bytes directly from the TUN interface file descriptor.
* **Supported Protocols:**
  * **IPv4:** Header length, total length, identification, flags, fragment offset, TTL, protocol number, checksum, and source/destination addresses.
  * **TCP:** Source/destination ports, sequence/acknowledgment numbers, data offset, flags (`SYN`, `ACK`, `FIN`, `RST`, `PSH`, `URG`, `ECE`, `CWR`), window size, and checksum.
  * **UDP:** Source/destination ports, length, and checksum.
  * **ICMP:** Type, code, checksum, ID, and sequence number (supports Echo Request/Reply, Destination Unreachable, Time Exceeded).
  * **DNS:** Transaction ID, QR flag, query counts, and query domain name decoding (including DNS pointer compression).
* **Hex Dump Viewer:** Formatted byte offset, hexadecimal byte values, and ASCII text representation for every captured packet.

### 3. Real-Time Dashboard
* **Hero Status Card:** Displays session duration, packet count, total data volume, and a rolling 30-point live traffic waveform.
* **Interactive Monitoring Control:** Custom animated capture button with breathing glow effects and rotating status text.
* **Live Metrics Grid:** 3×2 grid showing live metrics (Total Packets, TCP, UDP, DNS, Alerts, Data Volume) with 12-point mini sparkline graphs.
* **Protocol Distribution:** Multi-slice donut chart visualizing protocol ratios with smooth animated transitions.

### 4. Packet Filtering & Search
* **Search Filter:** Filter packets in real-time by IP address, port number, protocol name, DNS query domain, or info text.
* **Protocol Chips:** Multi-select filtering across TCP, UDP, DNS, TLS, ICMP, and HTTP.
* **Sorting Options:** Sort packet list by Newest First, Oldest First, Largest Size, or Protocol name.
* **Detail Drill-Down:** Collapsible glass cards for each protocol layer and raw packet payload inspection.

### 5. Traffic Analytics
* **Historical Throughput Graph:** 60-point historical traffic throughput chart.
* **Top Conversations:** Live rankings of source-to-destination conversations sorted by packet volume.
* **Top Destination IPs:** Most-contacted IP endpoints by packet count and byte volume.
* **Top Destination Ports:** Breakdown of destination ports mapped to standard service names (HTTP, HTTPS, DNS, SSH, etc.).
* **Top DNS Queries:** Frequency ranking of requested domain names.

### 6. Connection Tracking
* Correlates individual packets into bidirectional `IP:Port ↔ IP:Port` connection sessions.
* Tracks TCP lifecycle states (`SYN_SENT`, `SYN_RECEIVED`, `ESTABLISHED`, `FIN_WAIT`, `CLOSED`) and active UDP flows.
* Records cumulative bytes transferred (in/out), packet counts, session start time, and last activity timestamps.
* Categorized by protocol tabs (All, TCP, UDP, TLS) with search filtering.

### 7. Rule-Based Security Analysis
* **In-Memory Heuristic Engine:** Analyzes captured packets against heuristic threat rules:
  * **Cleartext Traffic Detection:** Flags unencrypted plaintext communication on sensitive ports (80, 21, 23, 25, 110, 143).
  * **Port Scan Detection:** Detects suspicious scanning activity when a single source contacts 20 or more distinct destination ports.
  * **Suspicious DNS Detection:** Identifies unusually long domain names (>60 characters) characteristic of DNS tunneling or data exfiltration.
* **Threat Severity Ratings:** Categorizes alerts by severity (Low, Medium, High, Critical) with dedicated alert cards and visual indicators.

### 8. Settings & Customization
* **Preferences Manager:** Persists configuration via `SharedPreferences`.
* **Configurable Settings:** Capture MTU, DNS servers, Subnet IP, Promiscuous mode, DNS resolution, Auto-save toggle, Glow intensity, Refresh interval, Audio alerts, Notification priority, Capture profile, and Reduced motion.
* **Input Validation:** In-app validation for IP addresses and MTU ranges with error handling and unsaved-changes protection dialogs.

---

## How It Works

```
┌────────────────────────────────────────────────────────┐
│                   Android OS Kernel                    │
└────────────────────────────────────────────────────────┘
                           │
                 [VpnService TUN Interface]
                     (10.0.0.2 / 0.0.0.0)
                           │
                           ▼
              ┌───────────────────────────┐
              │    VpnCaptureService      │
              │  (Foreground Service)     │
              └─────────────┬─────────────┘
                            │
               Reads raw IP bytes from TUN fd
                            │
                            ▼
              ┌───────────────────────────┐
              │       PacketParser        │
              │  (Pure Kotlin Decoder)    │
              └─────────────┬─────────────┘
                            │
               Parsed Packet data objects
                            │
                            ▼
              ┌───────────────────────────┐
              │   PacketRepositoryImpl    │
              │ (In-Memory Buffer: 10,000)│
              └─────────────┬─────────────┘
              │             │             │
        Aggregates    Tracks Active  Heuristic Threat
          Stats        Connections     Analysis
              │             │             │
              └─────────────┼─────────────┘
                            │
                 Reactive StateFlow Streams
                            │
                            ▼
              ┌───────────────────────────┐
              │     Jetpack Compose UI    │
              │ (Dashboard, Packets, etc.)│
              └───────────────────────────┘
```

1. **Activation:** The user initiates capture from the main screen. If VPN permission is not yet granted, the app launches `VpnService.prepare(context)`.
2. **TUN Setup:** `VpnCaptureService` creates a virtual network interface using `VpnService.Builder`, configuring the MTU, subnet IP, and DNS servers.
3. **Capture Loop:** A background coroutine continuously reads raw IPv4 packets from the TUN `FileInputStream`.
4. **Parsing:** `PacketParser` decodes headers into structured `Packet` models.
5. **UDP Forwarding:** UDP packets are forwarded to real destination addresses using protected sockets to preserve device network connectivity; incoming UDP responses are injected back into the TUN stream.
6. **State & UI:** Packets, metrics, active connections, and security alerts are emitted via `StateFlow` to the ViewModels, updating the Jetpack Compose screens reactively.

---

## Tech Stack & Dependencies

* **Language:** [Kotlin 2.0.21](https://kotlinlang.org)
* **Minimum SDK:** API 26 (Android 8.0 Oreo)
* **Target / Compile SDK:** API 35 (Android 15)
* **Build System:** Gradle 8.9 with Android Gradle Plugin (AGP) 8.7.2
* **UI Toolkit:** Jetpack Compose with Material 3
* **Concurrency:** Kotlinx Coroutines Android 1.9.0 (`StateFlow`, `SharedFlow`)
* **Architecture Components:**
  * `androidx.activity:activity-compose:1.9.3`
  * `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7`
  * `androidx.lifecycle:lifecycle-runtime-compose:2.8.7`
  * `androidx.navigation:navigation-compose:2.8.5`
  * `androidx.core:core-splashscreen:1.0.1`
  * `androidx.compose.ui:ui-text-google-fonts`
* **Visual Blur:** [`com.github.Dimezis:BlurView:version-2.0.6`](https://github.com/Dimezis/BlurView) for real-time backdrop blur behind navigation elements.

---

## Current Status & Known Limitations

To maintain full transparency regarding the current state of development:

1. **TCP Proxying:** While TCP packets passing through the TUN interface are captured, decoded, and analyzed, outbound TCP forwarding to remote destinations is currently stubbed. In an Android `VpnService` architecture, full TCP proxying requires an integrated userspace TCP/IP stack (such as `lwIP` or `tun2socks` via JNI). UDP traffic forwarding is functional.
2. **PCAP File Export/Import:** The PCAP management screen exists with an empty state and action buttons, but saving to `.pcap` files on disk and importing `.pcap` files from external storage are not yet implemented.
3. **DNS Answer Records:** DNS queries are parsed, but DNS answer record parsing is deferred.
4. **IPv6:** Only IPv4 packets are currently processed; IPv6 packets are filtered out during parsing.

---

## Project Structure

```
Pocketshark/
├── .gitignore
├── LICENSE
├── README.md
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew / gradlew.bat
├── gradle/wrapper/
├── screenshots/
│   └── README.md
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/pocketshark/app/
        │   ├── MainActivity.kt               # Single activity hosting Jetpack Compose UI
        │   ├── PocketSharkApp.kt             # Application class initializing notification channels
        │   ├── capture/
        │   │   ├── CaptureEngine.kt          # Capture interface contract
        │   │   ├── CaptureState.kt           # Capture state definitions
        │   │   ├── PacketParser.kt           # Pure-Kotlin IPv4/TCP/UDP/ICMP/DNS parser & hex dump
        │   │   └── VpnCaptureService.kt      # Android VpnService TUN interface implementation
        │   ├── data/
        │   │   ├── SettingsManager.kt        # SharedPreferences persistence manager
        │   │   ├── model/
        │   │   │   ├── CaptureSession.kt     # Metrics & PCAP file metadata models
        │   │   │   ├── Connection.kt         # Connection session & state models
        │   │   │   ├── Packet.kt             # Protocol enums & packet header models
        │   │   │   └── SecurityAlert.kt      # Threat severity & alert models
        │   │   └── repository/
        │   │       ├── PacketRepository.kt   # Repository interface
        │   │       └── PacketRepositoryImpl.kt # In-memory buffer, heuristics & connection tracker
        │   ├── ui/
        │   │   ├── components/               # Glassmorphic UI widgets (Buttons, Cards, Graphs)
        │   │   ├── navigation/               # Navigation routes, NavHost, and bottom nav items
        │   │   ├── screens/                  # 11 Compose screens (Capture, Packets, Analytics, etc.)
        │   │   └── theme/                    # Color tokens, Typography, Theme, Glass styling
        │   └── viewmodel/                    # Architecture ViewModels
        └── res/                              # Drawables, mipmaps, colors, font certificates
```

---

## Getting Started

### Prerequisites
* **Android Studio:** Ladybug (2024.2.1) or newer recommended.
* **JDK:** Java Development Kit 17 or higher (such as the JetBrains Runtime bundled with Android Studio).
* **Android SDK:** SDK Platform 35 (Android 15), Build-Tools 35.0.0.
* **Device / Emulator:** Android 8.0 (API level 26) or higher.

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/Shahbaz-ali22/PocketShark.git
   cd PocketShark
   ```

2. Open the project in **Android Studio** and allow Gradle to sync.

3. To assemble the debug APK via command line:
   ```bash
   # On Linux / macOS:
   ./gradlew assembleDebug

   # On Windows (PowerShell):
   .\gradlew.bat assembleDebug
   ```

4. Install directly to a connected device or running emulator:
   ```bash
   .\gradlew.bat installDebug
   ```

---

## Required Permissions

PocketShark declares the following permissions in `AndroidManifest.xml`:

| Permission | Purpose |
| :--- | :--- |
| `android.permission.INTERNET` | Required for opening protected forward sockets. |
| `android.permission.BIND_VPN_SERVICE` | Required to bind to the Android `VpnService` TUN interface. |
| `android.permission.FOREGROUND_SERVICE` | Required to run packet capture as an active background service. |
| `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | Complies with Android 14+ foreground service classification for packet capture. |
| `android.permission.POST_NOTIFICATIONS` | Required on Android 13+ to display ongoing capture notifications. |

---

## Roadmap

- [ ] Integrate a userspace TCP/IP stack (e.g., `tun2socks` or `lwIP`) to enable outbound TCP proxying.
- [ ] Implement PCAP file writing (`.pcap`) and file export to Android Shared Storage.
- [ ] Implement PCAP file reading and offline capture playback.
- [ ] Add IPv6 packet header decoding.
- [ ] Expand DNS answer record parsing.
- [ ] Add per-application traffic filtering using Android's `VpnService.Builder.addAllowedApplication`.

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## Author

**Shahbaz Ali**  
GitHub: [@Shahbaz-ali22](https://github.com/Shahbaz-ali22)
