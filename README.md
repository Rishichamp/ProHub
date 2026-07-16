# ProHub — Complete Setup Guide
## Prerequisites
Before building, you need:

| Requirement | Version | Where to Get |
|-------------|---------|-------------|
| Android Studio | Latest stable | [developer.android.com/studio](https://developer.android.com/studio) |
| JDK | 17+ | Bundled with Android Studio or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) |
| Android SDK | API 35 | Downloaded via Android Studio SDK Manager |
| Gemini API Key | Free tier | [Google AI Studio](https://aistudio.google.com/app/apikey) |

---

## Step 1: Get Your Gemini API Key (REQUIRED)

The AI features (chat, voice assistant responses, notification summarization) **will not work** without this key.

1. Go to [https://aistudio.google.com/app/apikey](https://aistudio.google.com/app/apikey)
2. Sign in with your Google account
3. Click **"Create API Key"**
4. Copy the key (starts with `AIzaSy...`)
5. You get **free tier**: 1,500 requests/day, 60 requests/minute

> **Security Note:** The key is stored encrypted in `EncryptedSharedPreferences` (AES-256-GCM). Never commit it to version control.

---

## Step 2: Project Setup

### 2.1 Unzip the Project

```bash
unzip ProHub_Complete_Project.zip -d ~/AndroidStudioProjects/
cd ~/AndroidStudioProjects/ProHub
```

### 2.2 Open in Android Studio

```
File → Open → Select the "ProHub" folder
```

Wait for Gradle sync (5-10 minutes on first open). If sync fails:
- **File → Invalidate Caches / Restart → Invalidate and Restart**
- Then **File → Sync Project with Gradle Files**

---

## Step 3: Add Mipmap Icons (REQUIRED for compilation)

The app references `ic_launcher` and `ic_launcher_round` in `AndroidManifest.xml`. You must create these:

1. In Android Studio, right-click `app/src/main/res`
2. **New → Image Asset**
3. **Icon Type:** Launcher Icons (Adaptive and Legacy)
4. **Asset:** Clip Art or Text (type "PH" for ProHub)
5. **Foreground Color:** `#6366F1` (Indigo)
6. **Background Color:** `#08090D` (Dark)
7. Click **Next → Finish**

This auto-generates:
- `mipmap-mdpi/ic_launcher.png` through `mipmap-xxxhdpi/ic_launcher.png`
- `mipmap-mdpi/ic_launcher_round.png` through `mipmap-xxxhdpi/ic_launcher_round.png`

---

## Step 4: Build & Run

### 4.1 Build the Project

```bash
./gradlew assembleDebug
```

Or in Android Studio: **Build → Make Project** (Ctrl+F9)

### 4.2 Run on Device/Emulator

- **Emulator:** Create a virtual device with API 26+ (Android 8.0)
- **Physical Device:** Enable USB debugging in Developer Options

Click the green **Run** button (▶) or press **Shift+F10**

---

## Step 5: First Launch Setup

### 5.1 Onboarding

The app starts with a permission onboarding screen:

| Permission | Why It's Needed | When to Grant |
|------------|---------------|---------------|
| **Microphone** | Voice commands, "Hey ProHub" wake word | On first launch |
| **Notifications** | Daily reminders, task alerts | On first launch |
| **Notification Listener** | Read & summarize app notifications | When navigating to Notifications tab |

> **Tip:** Grant Notification Listener access via **Settings → Apps → Special Access → Notification Access → ProHub**

### 5.2 Enter Your Gemini API Key

1. Complete onboarding → land on Home screen
2. Tap **Settings** (⚙️) in bottom navigation
3. Expand **"AI Configuration"** section
4. Paste your API key: `AIzaSy...`
5. Tap **"Save API Key"**
6. Green checkmark ✓ confirms it's saved

The key is now encrypted and persists across app restarts.

---

## Step 6: Test Core Features

### Voice Assistant
1. Say **"Hey ProHub"** (or tap microphone in AI tab)
2. Try: *"What's the weather?"*, *"Add task: Buy milk at 5 PM"*, *"Show my pending tasks"*

### Fitness Tracker
1. Tap **Fitness** (💪) in bottom nav
2. Tap **+** → select workout type → enter duration
3. View progress in "Goals" tab

### Task Management
1. Tap **Tasks** (📋) in bottom nav
2. Tap **+ FAB** → enter title → pick date/time with picker
3. Tap checkbox to complete (green animation)

### Intelligent Reminders
- The app checks every 15 minutes for overdue tasks
- Escalation levels: Gentle → Warning → Urgent → Critical
- Swipe notifications to snooze or mark complete

### AI Chat
1. Tap **AI** (🤖) in bottom nav
2. Type any question → hit send
3. See typing indicator → AI response appears

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `R.drawable.ic_launcher` not found | Complete Step 3 (Image Asset) |
| Gradle sync fails | File → Invalidate Caches → Restart |
| `Unresolved reference: hilt` | Check `build.gradle.kts` has `id("com.google.dagger.hilt.android")` |
| AI responses say "API key missing" | Complete Step 5.2 (enter Gemini key) |
| Voice commands not working | Grant Microphone permission in Android Settings |
| Notifications not captured | Enable Notification Listener in system settings |
| Reminders not firing | Check battery optimization is disabled for ProHub |
| Database migration crash | Uninstall app → reinstall (dev builds use destructive migration) |

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                  │
│  Home │ Tasks │ Timetable │ Fitness │ AI │ Notif │ Settings │
├─────────────────────────────────────────────────────────┤
│                 ViewModel Layer (StateFlow)              │
├─────────────────────────────────────────────────────────┤
│               Repository Layer (Business Logic)          │
├─────────────────────────────────────────────────────────┤
│  Room DB (SQLCipher) │ EncryptedSharedPreferences │ API  │
├─────────────────────────────────────────────────────────┤
│  Services: FloatingBubble │ NotificationListener │ Workers │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.0.20 |
| UI | Jetpack Compose (BOM 2024.09) |
| Architecture | MVVM + Repository + Clean Architecture |
| DI | Hilt 2.52 |
| Database | Room 2.6.1 + SQLCipher 4.9.0 (encrypted) |
| Background | WorkManager 2.9.1 + AlarmManager |
| Networking | OkHttp 4.12.0 |
| AI | Google Gemini 2.0 Flash API |
| Security | AES-256-GCM (EncryptedSharedPreferences) |

---

## License

MIT License — Open source, free to use and modify.

Built with ❤️ for productivity enthusiasts.
