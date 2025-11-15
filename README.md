# 🔒 BlockerApp - Android App Locker with TOTP

A hackathon-ready Android application that locks installed apps behind manual or TOTP-based authentication. Built for speed and functionality over security.

## 🚀 Features

### ✅ Basic App Locking
- Display all installed apps in a searchable list
- Toggle any app to lock/unlock it
- Locked apps show a fullscreen blocking screen when launched
- Manual unlock with temporary access (5 minutes)

### 🔐 TOTP-Based Locking
- Generate random Base32 secret keys
- Display 6-digit TOTP codes that update every 30 seconds
- Import/export secret keys between devices
- Two-device authentication workflow (Phone A ↔ Phone B)
- Code verification with ±30 second tolerance

## 📱 Quick Start

### Prerequisites
- Android Studio Arctic Fox or newer
- Android device/emulator running API 26+ (Android 8.0+)
- Basic knowledge of Android permissions

### Installation

1. **Clone/Open the Project**
   ```bash
   cd C:\Users\ilyas\StudioProjects\blockerapp
   ```

2. **Open in Android Studio**
   - File > Open > Select `blockerapp` folder
   - Wait for Gradle sync

3. **Run the App**
   - Click the "Run" button (or Shift+F10)
   - Select your device/emulator

4. **Grant Permissions**
   - **Accessibility Service**: Settings > Accessibility > BlockerApp > Enable
   - **Overlay Permission**: Settings > Apps > BlockerApp > Display over other apps > Allow

## 📖 Usage Guide

### Locking an App (Manual Mode)

1. Open BlockerApp
2. Find the app you want to lock (e.g., Instagram)
3. Toggle the switch ON
4. Try opening Instagram → Blocking screen appears
5. Click "Unlock App" to temporarily unlock (5 min)

### TOTP Setup (Two-Device Mode)

**On Phone B (Code Generator):**
1. Open BlockerApp
2. Tap the key icon (floating button)
3. Tap "Generate" → Copy the secret key
4. Send via SMS to Phone A
5. View the current 6-digit code

**On Phone A (Protected Device):**
1. Open BlockerApp
2. Toggle the app lock ON
3. Tap the settings icon next to the app
4. Paste the secret key from Phone B
5. Tap "Import from Field"
6. Tap "Enable TOTP"

**To Unlock:**
1. Try opening the locked app
2. Get current code from Phone B
3. Enter the code on Phone A
4. Tap "Verify & Unlock"

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin
- **UI**: Material Design 3, ViewBinding
- **Storage**: SharedPreferences + Gson (JSON)
- **TOTP**: Apache Commons Codec (Base32 + HMAC-SHA1)
- **Monitoring**: AccessibilityService

### Key Components

| Component | Purpose |
|-----------|---------|
| `AccessibilityService` | Detects app launches in real-time |
| `BlockerOverlayActivity` | Fullscreen lock screen |
| `TotpManager` | Generate/verify 6-digit codes |
| `PreferencesHelper` | Persist locked apps and secrets |
| `MainActivity` | App list with lock toggles |

### Data Flow

```
User opens locked app
    ↓
AccessibilityService detects launch
    ↓
Check if app is in locked list
    ↓
Launch BlockerOverlayActivity (fullscreen)
    ↓
User enters TOTP code (or clicks unlock)
    ↓
Verify code → Unlock temporarily
    ↓
Return to home screen
```

## 📂 Project Structure

```
app/src/main/
├── java/com/hackathon/blockerapp/
│   ├── BlockerApplication.kt
│   ├── models/
│   │   └── LockedApp.kt
│   ├── service/
│   │   └── AppBlockerAccessibilityService.kt
│   ├── ui/
│   │   ├── MainActivity.kt
│   │   ├── BlockerOverlayActivity.kt
│   │   ├── TotpActivity.kt
│   │   └── adapters/
│   │       └── AppListAdapter.kt
│   └── utils/
│       ├── TotpManager.kt
│       ├── PreferencesHelper.kt
│       └── PermissionHelper.kt
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   ├── activity_blocker_overlay.xml
│   │   ├── activity_totp.xml
│   │   └── item_app.xml
│   ├── drawable/
│   │   ├── ic_lock.xml
│   │   ├── ic_lock_open.xml
│   │   ├── ic_key.xml
│   │   └── ic_settings.xml
│   └── xml/
│       └── accessibility_service_config.xml
└── AndroidManifest.xml
```

## 🎯 Implementation Roadmap (24 Hours)

| Phase | Hours | Deliverable |
|-------|-------|-------------|
| **Setup & Basic UI** | 0-3 | App list with toggles |
| **Locking Mechanism** | 3-8 | AccessibilityService + Blocker screen |
| **TOTP Foundation** | 8-12 | Secret generation + code display |
| **TOTP Integration** | 12-16 | Code verification + app linking |
| **Polish & Testing** | 16-22 | Search, permissions, bug fixes |
| **Demo Prep** | 22-24 | Testing, documentation |

## ⚡ Hackathon Shortcuts

To meet the 24-hour deadline, this app uses several "bad practice" shortcuts:

1. ❌ **No Encryption**: Secrets stored in plain SharedPreferences
2. ❌ **No Input Validation**: Assumes users enter valid data
3. ❌ **Global Context**: `BlockerApplication.instance` used everywhere
4. ❌ **No Unit Tests**: Zero test coverage
5. ❌ **Hardcoded Values**: 30-second TOTP window, 5-minute unlock
6. ❌ **Minimal Error Handling**: Crashes reveal bugs quickly

**⚠️ DO NOT USE IN PRODUCTION!**

## 🐛 Known Issues

- Can be bypassed by disabling accessibility service
- Battery drain from continuous monitoring
- No encryption for stored secrets
- Requires manual permission setup
- Some devices kill the service aggressively

## 🔧 Troubleshooting

### "App doesn't block anything"
→ Enable Accessibility Service in Settings

### "TOTP codes don't match"
→ Ensure both devices have correct time (auto-sync enabled)

### "Service keeps stopping"
→ Add to battery optimization whitelist

### Build errors
```bash
./gradlew clean
./gradlew build
```

## 📚 Documentation

- **[ARCHITECTURE.md](ARCHITECTURE.md)**: Detailed architecture and design decisions
- **[IMPLEMENTATION.md](IMPLEMENTATION.md)**: Step-by-step implementation guide
- **[prompt.md](prompt.md)**: Original requirements

## 🧪 Testing

```bash
# Manual test checklist
- [ ] List apps
- [ ] Lock an app
- [ ] App shows blocker screen
- [ ] Manual unlock works
- [ ] Generate secret key
- [ ] Copy to clipboard
- [ ] Import secret key
- [ ] TOTP code updates
- [ ] Enable TOTP for app
- [ ] Verify code unlocks app
- [ ] Search works
```

## 📦 Dependencies

```gradle
// Core Android
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0

// JSON parsing
com.google.code.gson:gson:2.10.1

// TOTP (Base32 + HMAC)
commons-codec:commons-codec:1.16.0
```

## 🎓 Learning Resources

- [RFC 6238 - TOTP Specification](https://tools.ietf.org/html/rfc6238)
- [Android Accessibility Service](https://developer.android.com/guide/topics/ui/accessibility/service)
- [Material Design 3](https://m3.material.io/)

## 🚀 Future Enhancements

- [ ] Use Android Keystore for encryption
- [ ] QR code generation for secret sharing
- [ ] Biometric authentication
- [ ] App usage statistics
- [ ] Custom unlock durations
- [ ] Export/import settings
- [ ] Dark theme

## 👥 Contributing

This is a hackathon project - feel free to fork and improve!

## 📄 License

MIT License - Use freely for hackathons and learning!

---

**Built for a 24-hour hackathon** 🏆
**Focus**: Working code over clean code ⚡
**Status**: Feature-complete MVP ✅
### **Algorithm: TOTP (RFC 6238)**
- **Library**: Use `commons-codec` or `Google Authenticator-compatible` libraries
- **Key**: 160-bit secret key shared between devices
- **Time Step**: 30 seconds
- **Code Length**: 6 digits
- **Algorithm**: HMAC-SHA1

### **Pairing Process:**
1. Phone B generates a random secret key
2. Display as QR code (contains base32-encoded secret)
3. Phone A scans QR code and stores secret
4. Both phones now generate identical codes using current time

### **No Backend Needed:**
- Both devices use their system time + shared secret
- Codes sync automatically (30-second windows provide tolerance)
- No internet/messaging required after initial pairing

---

## **3. Required Android Permissions & APIs**

### **Phone A (Blocker):**
```
- BIND_ACCESSIBILITY_SERVICE (detect app launches)
- SYSTEM_ALERT_WINDOW (overlay lock screen)
- PACKAGE_USAGE_STATS (track app usage)
- CAMERA (for QR code scanning during pairing)
```

### **Phone B (Generator):**
```
- CAMERA (optional, for displaying QR code)
```

### **Critical Limitations:**
- **No true "locking"**: Android doesn't allow apps to prevent other apps from launching
- **Workaround**: Immediately cover the app with an overlay when detected
- **Users can bypass** via Safe Mode or uninstalling (by design, Android security)
- **Accessibility Service** can be disabled by users in settings

---

## **4. Recommended Tech Stack**

### **Development Tools:**
- **IDE**: Android Studio (correct choice - industry standard)
- **Language**: **Kotlin** (recommended) or Java
- **Min SDK**: API 23 (Android 6.0) for UsageStatsManager
- **Target SDK**: Latest stable (API 34/35)

### **Key Libraries:**
```gradle
// TOTP Generation
implementation 'commons-codec:commons-codec:1.15'
implementation 'de.taimos:totp:1.0'

// QR Code Generation/Scanning
implementation 'com.google.zxing:core:3.5.1'
implementation 'com.journeyapps:zxing-android-embedded:4.3.0'

// Secure Storage
// (Built-in Android Keystore)

// UI
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.11.0'
```

---

## **5. System Architecture Diagram**

```
┌─────────────────────────────────────────────────────────┐
│                      PHONE A (BLOCKER)                  │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────────┐      ┌────────────────────┐     │
│  │ Accessibility    │─────▶│ Overlay Lock       │     │
│  │ Service          │      │ Screen             │     │
│  │ (Detects Launch) │      │                    │     │
│  └──────────────────┘      │  [Enter TOTP Code] │     │
│           │                │  [___][___][___]   │     │
│           ▼                │  [___][___][___]   │     │
│  ┌──────────────────┐      └────────┬───────────┘     │
│  │ Usage Stats      │               │                  │
│  │ Monitor          │               ▼                  │
│  └──────────────────┘      ┌────────────────────┐     │
│                             │ TOTP Validator     │     │
│                             │ (Local Check)      │     │
│                             └────────┬───────────┘     │
│                                      │                  │
│                             ┌────────▼───────────┐     │
│                             │ Keystore           │     │
│                             │ (Shared Secret)    │     │
│                             └────────────────────┘     │
└─────────────────────────────────────────────────────────┘
                                    ▲
                                    │ QR Code Pairing
                                    │
┌───────────────────────────────────┴─────────────────────┐
│                      PHONE B (GENERATOR)                │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────────────────────────────────┐          │
│  │          TOTP Generator UI               │          │
│  │                                           │          │
│  │         Current Code: 482 916             │          │
│  │         Refreshes in: 12s                 │          │
│  │                                           │          │
│  │         [Show QR Code for Pairing]        │          │
│  └──────────────────┬────────────────────────┘          │
│                     │                                    │
│                     ▼                                    │
│  ┌──────────────────────────────────────────┐          │
│  │ TOTP Engine (RFC 6238)                   │          │
│  │ - Time: System.currentTimeMillis()       │          │
│  │ - Secret: Stored in Keystore             │          │
│  │ - Output: 6-digit code                   │          │
│  └──────────────────────────────────────────┘          │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## **6. Step-by-Step Implementation Roadmap**

### **Phase 1: Setup & Basic Structure (Week 1)**
1. Install Android Studio
2. Create new Android project (Kotlin, Empty Activity)
3. Set up Git repository
4. Add required dependencies to `build.gradle`
5. Request permissions in `AndroidManifest.xml`

### **Phase 2: TOTP Core (Week 1-2)**
1. Implement TOTP generation algorithm
2. Create shared secret generator
3. Build QR code generation (Phone B)
4. Build QR code scanner (Phone A)
5. Test code synchronization between devices

### **Phase 3: App Selection UI (Week 2)**
1. Fetch installed apps list using `PackageManager`
2. Create UI to select apps to lock
3. Store locked app list in `SharedPreferences` or Room DB
4. Display locked apps with toggle switches

### **Phase 4: Locking Mechanism (Week 3-4)**
1. Create `AccessibilityService` to monitor app launches
2. Implement overlay lock screen Activity
3. Add TOTP input validation
4. Handle unlock logic
5. Test with various apps (Instagram, TikTok, etc.)

### **Phase 5: Polish & Edge Cases (Week 4-5)**
1. Handle time drift between devices (accept ±1 time window)
2. Add haptic feedback and animations
3. Implement settings screen
4. Handle system events (reboot, service killed)
5. Battery optimization whitelist prompt

### **Phase 6: Testing (Week 5-6)**
1. Test on physical devices (emulator limitations with accessibility)
2. Test time zone changes
3. Test network disconnection scenarios
4. User acceptance testing

---

## **7. Critical Implementation Details**

### **AccessibilityService Example Structure:**
```kotlin
class AppLockService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if (isAppLocked(packageName)) {
                showLockScreen(packageName)
            }
        }
    }
}
```

### **TOTP Generation:**
```kotlin
fun generateTOTP(secret: String): String {
    val time = System.currentTimeMillis() / 1000 / 30
    val hash = HmacSHA1(secret, time)
    val offset = hash[hash.size - 1].toInt() and 0xF
    val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                 ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                 ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                 (hash[offset + 3].toInt() and 0xFF)
    val otp = binary % 1000000
    return String.format("%06d", otp)
}
```

---

## **8. Security Considerations**

### **Not Super Secure (As You Noted):**
- Shared secret transmitted via QR code (offline, but visible)
- No encryption between devices (not needed since no communication)
- User can uninstall or disable the service
- Time-based, so predictable 30-second windows

### **Good Enough For Your Use Case:**
- Prevents casual app access (accountability buddy system)
- No server infrastructure = no data leaks
- Friend controls the "key" on their device
- Simple and functional for self-discipline

### **Improvements (Optional):**
- Add biometric unlock as backup
- Implement tamper detection
- Add usage statistics/reports

---

## **9. Potential Challenges & Solutions**

| Challenge | Solution |
|-----------|----------|
| Service gets killed by system | Use foreground service with notification |
| Battery drain from monitoring | Optimize event listening, use JobScheduler |
| Time drift between devices | Accept ±1 time window (90-second tolerance) |
| User disables accessibility | Persistent notification + re-enable prompts |
| Apps with multiple entry points | Monitor all activities, not just package names |
| Overlay permission denied | Request in onboarding flow |

---

## **10. Alternative Approaches**

### **Instead of TOTP, Consider:**
- **Bluetooth Proximity**: Unlock only when Phone B is nearby
- **Cloud-based Keys**: Firebase Realtime Database for key sync
- **NFC Tags**: Tap Phone B to Phone A to unlock

### **For Development:**
- Android Studio is the best choice (no better alternatives)
- Use emulator for initial development, but **must test on physical devices** for Accessibility features

---

## **Getting Started - First Steps**

1. **Install Android Studio** from developer.android.com
2. **Create project**: "Empty Views Activity", Language: Kotlin
3. **Create two build variants** (or two separate apps):
   - `blocker` (Phone A app)
   - `generator` (Phone B app)
4. **Start with TOTP**: Get the key generation and validation working first
5. **Then add UI**: Build the app selection and lock screen
6. **Finally AccessibilityService**: This is the trickiest part

---

Would you like me to create a starter project structure with the initial setup files, or would you like more detail on any specific component? I can also generate sample code for the TOTP implementation or the AccessibilityService!
