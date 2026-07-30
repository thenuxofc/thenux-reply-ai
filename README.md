<div align="center">

<img src="./assets/banner.svg" alt="THENUX Reply AI Banner" width="100%" />

<br/>

# 🟣 THENUX Reply AI

### Context-Aware, Tri-Lingual Smart Reply Assistant for Android

**Floating AI overlay · Accessibility-powered chat reading · Instant multi-tone replies**

<br/>

<img src="https://img.shields.io/badge/Platform-Android-6C4CE0?style=for-the-badge&logo=android&logoColor=00F0FF&labelColor=0D0D14" />
<img src="https://img.shields.io/badge/Kotlin-100%25-6C4CE0?style=for-the-badge&logo=kotlin&logoColor=00F0FF&labelColor=0D0D14" />
<img src="https://img.shields.io/badge/UI-Jetpack%20Compose-6C4CE0?style=for-the-badge&logo=jetpackcompose&logoColor=00F0FF&labelColor=0D0D14" />
<img src="https://img.shields.io/badge/Status-Release%20Ready-00F0FF?style=for-the-badge&labelColor=0D0D14" />

<br/>

<img src="https://img.shields.io/github/stars/thenuxofc/thenux-reply-ai?style=flat-square&color=6C4CE0&labelColor=0D0D14" />
<img src="https://img.shields.io/github/last-commit/thenuxofc/thenux-reply-ai?style=flat-square&color=00F0FF&labelColor=0D0D14" />
<img src="https://img.shields.io/github/license/thenuxofc/thenux-reply-ai?style=flat-square&color=6C4CE0&labelColor=0D0D14" />
<img src="https://img.shields.io/badge/Made%20by-THENUX-6C4CE0?style=flat-square&labelColor=0D0D14" />

<br/><br/>

<a href="#-key-features"><b>Features</b></a> ·
<a href="#-architecture"><b>Architecture</b></a> ·
<a href="#-permissions"><b>Permissions</b></a> ·
<a href="#-installation"><b>Install</b></a> ·
<a href="#-play-store-checklist"><b>Play Store Checklist</b></a> ·
<a href="#-tech-stack"><b>Tech Stack</b></a>

</div>

<br/>

<div align="center">
<img src="./assets/divider.svg" width="100%" />
</div>

<br/>

## ⚡ Overview

**THENUX Reply AI** is a floating, always-on-top Android assistant that reads incoming chat context on-device and instantly proposes six ready-to-send replies across different tones and languages — Friendly, Professional, Sinhala, Singlish, Short, and Formal. It runs quietly in the background via an `AccessibilityService` and `NotificationListenerService`, surfacing suggestions the moment a message lands, with zero manual copy-pasting required.

Built entirely with **Jetpack Compose**, **Kotlin Coroutines**, and a reactive **MVVM** architecture, the app ships with a floating bubble overlay, a home-screen widget, system-wide text-selection integration, and a tri-lingual context analysis engine tuned for Sinhala-speaking users.

<br/>

## ✨ Key Features

<table>
<tr>
<td width="50%" valign="top">

### 🫧 Floating AI Assistant Bubble
`ThenuxFloatingBubbleService`

- Always-on-top, unobtrusive overlay across any app
- **Paste & Reply** — paste clipboard text or pull the live detected message directly into the bubble
- Multi-model contextual analysis generates **6 instant replies**:
  - 😊 Friendly · 💼 Professional · 🇱🇰 Sinhala
  - 💬 Singlish · ⚡ Short · 👔 Formal
- One-tap copy to clipboard with instant toast confirmation
- Configurable size, position memory, and auto-clipboard toggle

</td>
<td width="50%" valign="top">

### 👁️ Background Context Reader
`ThenuxChatAccessibilityService`

- Monitors active messaging apps (e.g. WhatsApp) via `AccessibilityService`
- Smart thread parser auto-captures sender name + latest message
- Zero manual entry — fully automatic detection
- Real-time sync into the bubble and dashboard via Kotlin `StateFlow`

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 🔔 Notification Assistant
`ThenuxNotificationListenerService`

- Sniffs incoming chat notifications from supported apps
- Generates quick AI auto-reply proposals straight from the notification shade
- No need to open the source app at all

</td>
<td width="50%" valign="top">

### 🧠 Context AI Analysis Engine
`ReplyRepository`

- Intent & sentiment detection — urgency, action items, open questions, meeting requests, location asks
- Native tri-lingual support: **Sinhala**, **Singlish** (Latin-script Sinhala), and **English**
- Tuned specifically for natural Sri Lankan conversational patterns

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 🔗 System Text Selection & Share
`PROCESS_TEXT` / `SEND` intents

- Highlight any text anywhere on Android — Chrome, Gmail, anywhere
- Select **"THENUX AI"** from the system context menu or share sheet
- Instantly generates smart replies for the selected text

</td>
<td width="50%" valign="top">

### 📲 Home Screen Widget
`ThenuxQuickWidgetProvider`

- Native Android home-screen widget
- One-tap access to launcher actions
- Quick AI analysis without opening the full app

</td>
</tr>
</table>

<br/>

<div align="center">
<img src="./assets/divider.svg" width="100%" />
</div>

<br/>

## 🏗️ Architecture

<div align="center">
<img src="./assets/architecture.svg" width="100%" alt="Architecture Diagram" />
</div>

```mermaid
flowchart TB
    subgraph INPUT["📥 Input Sources"]
        A1["💬 Messaging App<br/>(WhatsApp, etc.)"]
        A2["🔔 System Notification"]
        A3["📋 Highlighted Text<br/>(Share Sheet / Context Menu)"]
    end

    subgraph CAPTURE["🎯 Capture Layer"]
        B1["ThenuxChatAccessibilityService"]
        B2["ThenuxNotificationListenerService"]
        B3["PROCESS_TEXT Intent Handler"]
    end

    subgraph CORE["🧠 Core Engine"]
        C1["ReplyRepository<br/>Intent & Sentiment Analysis"]
        C2["Tri-Lingual Processor<br/>Sinhala · Singlish · English"]
        C3["MainViewModel<br/>StateFlow"]
    end

    subgraph OUTPUT["📤 Output Surfaces"]
        D1["🫧 Floating Bubble<br/>6 Tone Suggestions"]
        D2["📲 Home Widget"]
        D3["📋 Clipboard + Toast"]
    end

    A1 --> B1
    A2 --> B2
    A3 --> B3
    B1 --> C1
    B2 --> C1
    B3 --> C1
    C1 --> C2
    C2 --> C3
    C3 --> D1
    C3 --> D2
    D1 --> D3
```

<br/>

## 🛡️ Permissions

| Permission | Manifest Constant | Purpose |
|---|---|---|
| 🌐 Internet Access | `android.permission.INTERNET` | Connects to AI models and cloud services for context analysis |
| 🪟 Display Over Other Apps | `android.permission.SYSTEM_ALERT_WINDOW` | Renders the floating assistant bubble on top of other apps |
| ⚙️ Foreground Service | `android.permission.FOREGROUND_SERVICE` | Keeps the floating assistant running reliably in the background |
| ♿ Accessibility Service | `BIND_ACCESSIBILITY_SERVICE` | Reads on-screen text in active messaging apps to detect received messages |
| 🔔 Notification Listener | `BIND_NOTIFICATION_LISTENER_SERVICE` | Detects incoming chat notifications to generate instant smart replies |

> **⚠️ Accessibility Disclosure:** `AccessibilityService` is used **exclusively on-device** to read incoming chat text for generating real-time contextual AI replies with user consent. No screen content is stored or transmitted beyond what's required for reply generation. See [Privacy Policy](#) for full details.

<br/>

<div align="center">
<img src="./assets/divider.svg" width="100%" />
</div>

<br/>

## 🧰 Tech Stack

<div align="center">

<img src="https://img.shields.io/badge/Kotlin-6C4CE0?style=for-the-badge&logo=kotlin&logoColor=00F0FF&labelColor=0D0D14" />
<img src="https://img.shields.io/badge/Jetpack%20Compose-6C4CE0?style=for-the-badge&logo=jetpackcompose&logoColor=00F0FF&labelColor=0D0D14" />
<img src="https://img.shields.io/badge/Material%20Design%203-6C4CE0?style=for-the-badge&logo=materialdesign&logoColor=00F0FF&labelColor=0D0D14" />
<img src="https://img.shields.io/badge/MVVM-6C4CE0?style=for-the-badge&labelColor=0D0D14&logoColor=00F0FF" />
<img src="https://img.shields.io/badge/Coroutines%20%2F%20StateFlow-6C4CE0?style=for-the-badge&logo=kotlin&logoColor=00F0FF&labelColor=0D0D14" />

</div>

- **UI Framework:** 100% Jetpack Compose with Material Design 3
- **State Management:** Kotlin Coroutines, `StateFlow`, MVVM (`MainViewModel`)
- **Design:** Responsive edge-to-edge layout, dark/light theme adaptation, rounded surface cards, animated interactive elements
- **AI Layer:** Multi-model contextual analysis (GPT-4 / Gemini–inspired reasoning pipeline)

<br/>

## 📦 Installation

```bash
git clone https://github.com/thenuxofc/thenux-reply-ai.git
cd thenux-reply-ai
```

Open in **Android Studio** (Hedgehog or newer) → let Gradle sync → run on a device or emulator (API 26+ recommended for full `AccessibilityService` + overlay support).

On first launch, grant the following when prompted:
1. **Display over other apps** — required for the floating bubble
2. **Accessibility Service** — required for automatic chat detection
3. **Notification access** — required for notification-based reply suggestions

<br/>

## 🏬 Play Store Checklist

Before publishing publicly, confirm the following:

- [ ] **Accessibility Disclosure** — prominent in-app disclosure + published Privacy Policy URL explaining `AccessibilityService` usage
- [ ] **Overlay Permission Flow** — `SYSTEM_ALERT_WINDOW` request UX tested on first launch
- [ ] **Branding Check** — launcher label in `res/values/strings.xml` matches the desired public store title
- [ ] **Release Signing** — production-signed APK/AAB generated via Android Studio with your production Keystore

<br/>

<div align="center">
<img src="./assets/divider.svg" width="100%" />
</div>

<br/>

<div align="center">

### Built with obsessive attention to detail by **THENUX**

<img src="https://img.shields.io/badge/Portfolio-thenuxofc.store-0D0D14?style=for-the-badge&logo=vercel&logoColor=00F0FF" />
<img src="https://img.shields.io/badge/GitHub-thenuxofc-0D0D14?style=for-the-badge&logo=github&logoColor=00F0FF" />

<sub>© 2026 THENUX. All rights reserved.</sub>

</div>
