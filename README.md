<div align="center">

# ProHub

**Your AI-Powered Professional Command Center**

An Android productivity assistant that brings tasks, schedule, fitness tracking, notifications, and a Gemini-powered voice/chat assistant together in one app — encrypted local storage, Material 3 design, and a custom neural-hub brand identity.

</div>

---

## Screenshots

<!--
  Add your screenshots here! Suggested shots: Home dashboard, AI Chat, Todos, Settings.
  Place image files in a `screenshots/` folder at the repo root, then reference them like:
  <img src="screenshots/home.png" width="220" />
-->

| Home | AI Chat | Todos | Settings |
|---|---|---|---|
| _add screenshot_ | _add screenshot_ | _add screenshot_ | _add screenshot_ |

---

## Features

- **AI Assistant** — chat with Gemini directly in-app, or activate hands-free with a "Hey ProHub" voice wake word via a floating overlay bubble
- **Task Management** — add, complete, and track tasks with due dates and times, backed by an encrypted local database
- **Daily Reminders** — a customizable daily reminder (user-chosen time, not hardcoded) nudges you to review your day, with both a notification and a spoken voice line
- **Schedule** — a lightweight timetable for classes, meetings, and recurring activities
- **Fitness Tracking** — log activity minutes, reflected live on the home dashboard
- **Notification Digest** — securely summarizes notifications from other apps (opt-in, via Android's Notification Listener API)
- **Secure by design** — app data is encrypted at rest with SQLCipher, and sensitive preferences (API keys, etc.) use Android's `EncryptedSharedPreferences`
- **PIN-protected settings** — a lightweight auth layer gates access to sensitive configuration
- **Custom brand identity** — a bespoke neural-hub P+H logomark, adaptive app icon, and a cohesive indigo/purple design language throughout

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Hilt (dependency injection) |
| Persistence | Room, encrypted with SQLCipher |
| Secure storage | AndroidX Security Crypto (`EncryptedSharedPreferences`) |
| Background work | WorkManager (Hilt-integrated `HiltWorkerFactory`) |
| AI | Google Gemini API |
| Networking | OkHttp |
| Async | Kotlin Coroutines & Flow |

---

## Architecture

ProHub follows a standard MVVM structure with Hilt-driven dependency injection:

```
ui/            Jetpack Compose screens, organized by feature (home, todos, timetable, fitness, ai, settings, auth, onboarding)
ui/components/ Shared, reusable Compose components (IconBadge, EmptyState, animated loaders, etc.)
ui/theme/      Design system — colors, typography, and the ProHub logo composable
ui/animation/  Reusable entrance/motion animations
data/          Room entities, DAOs, and repositories
data/prefs/    Encrypted preference storage
di/            Hilt modules
service/       Background services — floating voice bubble, reminder scheduling, notification listener
worker/        WorkManager workers (Hilt-integrated)
```

---

## Prerequisites

Before building, you need:

| Requirement | Version | Where to Get |
|---|---|---|
| Android Studio | Latest stable | [developer.android.com/studio](https://developer.android.com/studio) |
| JDK | 17+ | Bundled with Android Studio, or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) |
| Android SDK | API 35 | Downloaded via Android Studio SDK Manager |
| Gemini API Key | Free tier | [Google AI Studio](https://aistudio.google.com/apikey) |

## Getting Started

1. Clone the repo:
   ```bash
   git clone https://github.com/Rishichamp/ProHub.git
   ```
2. Open the project in Android Studio and let Gradle sync.
3. Run the app on an emulator or physical device.
4. On first launch, grant the requested permissions (microphone, notifications) and set a PIN.
5. In **Settings → AI Configuration**, paste your Gemini API key to enable the AI Assistant and voice features.

---

## License

This project is available under the MIT License — see [LICENSE](LICENSE) for details.

---

<div align="center">
<sub>Built as a personal project exploring Compose, secure on-device storage, and AI integration on Android.</sub>
</div>
