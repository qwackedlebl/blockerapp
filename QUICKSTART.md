# BlockerApp - Quick Start Guide

## 🚀 Build & Run (5 Minutes)

### Step 1: Open in Android Studio

1. Launch **Android Studio**
2. Click **"Open"** (or File > Open)
3. Navigate to: `C:\Users\ilyas\StudioProjects\blockerapp`
4. Click **OK**
5. Wait for Gradle sync (first time: ~2-5 minutes)

### Step 2: Run on Emulator

**Option A: Use Existing Emulator**
1. Click the device dropdown (top toolbar)
2. Select an existing emulator (API 26+)
3. Click the green **Run** button (▶)

**Option B: Create New Emulator**
1. Click device dropdown > **Device Manager**
2. Click **"Create Device"**
3. Select **Pixel 4** or any phone
4. Choose system image: **API 30** (Android 11) or higher
5. Click **Finish**
6. Click the green **Run** button (▶)

### Step 3: Grant Permissions (CRITICAL!)

The app will install but **won't work** until you grant permissions:

#### A. Enable Accessibility Service
```
Emulator Settings (gear icon)
  ↓
Accessibility
  ↓
BlockerApp
  ↓
Toggle ON
  ↓
Confirm "Allow"
```

#### B. Enable Overlay Permission
```
Emulator Settings
  ↓
Apps
  ↓
See all apps
  ↓
BlockerApp
  ↓
Display over other apps
  ↓
Toggle ON
```

### Step 4: Test Basic Locking

1. Open **BlockerApp**
2. Find **"Clock"** or **"Calculator"** in the list
3. Toggle the **switch** to lock it
4. Press **Home** button
5. Open the **Clock/Calculator** app
6. **Result**: Black screen appears → Click "Unlock App"

✅ **Success!** Basic locking works.

### Step 5: Test TOTP (Optional)

1. In BlockerApp, tap the **key icon** (bottom-right)
2. Tap **"Generate"**
3. You'll see: `JBSWY3DPEHPK3PXP` (example secret)
4. Current code displays: **"123456"** (changes every 30s)
5. Tap **Back**
6. Find an app → Toggle lock ON
7. Tap the **settings icon** next to it
8. Paste the secret key
9. Tap **"Import from Field"**
10. Tap **"Enable TOTP"**
11. Try opening the app → Enter the code

✅ **Success!** TOTP protection works.

---

## 🛠️ Troubleshooting

### "Gradle Sync Failed"
```bash
# In PowerShell/Terminal:
cd C:\Users\ilyas\StudioProjects\blockerapp
.\gradlew clean
.\gradlew build
```

### "Cannot resolve symbol"
```
File > Invalidate Caches... > Invalidate and Restart
```

### "App installs but doesn't block"
→ Did you enable **Accessibility Service**? (See Step 3A)

### "Overlay doesn't appear"
→ Did you grant **Display over other apps**? (See Step 3B)

### "App crashes on launch"
1. Check **Logcat** (bottom panel in Android Studio)
2. Look for red error lines
3. Common issue: Missing mipmap resources (fixed below)

---

## 📱 Testing on Physical Device

### Enable Developer Options
1. Settings > About phone
2. Tap **"Build number"** 7 times
3. Go back → **Developer options** appears

### Enable USB Debugging
1. Settings > Developer options
2. Toggle **"USB debugging"** ON
3. Connect phone via USB
4. Allow debugging on phone

### Install & Run
1. Select your device in Android Studio dropdown
2. Click **Run** (▶)
3. Grant permissions (same as emulator)

---

## 🎯 Demo Scenario (Hackathon Judges)

**Setup (1 minute):**
1. Open BlockerApp
2. Lock Instagram/WhatsApp/Chrome
3. Show it blocks immediately

**TOTP Demo (2 minutes):**
1. Generate secret key
2. Show code updating every 30 seconds
3. Enable TOTP for Instagram
4. Try to open Instagram
5. Enter wrong code → Blocked
6. Enter correct code → Unlocked

**Explain:**
- "Uses Accessibility Service to detect app launches"
- "TOTP generates RFC 6238-compliant codes"
- "Temporary unlock for 5 minutes"
- "Built in 24 hours for the hackathon"

---

## 📊 Project Statistics

```
Total Files: 40+
Lines of Code: ~2,000
Languages: Kotlin (95%), XML (5%)
Build Time: ~30 seconds
APK Size: ~2 MB
Min Android: 8.0 (API 26)
Target Android: 14 (API 34)
```

---

## 🔧 Build Commands

```bash
# Clean project
.\gradlew clean

# Build debug APK
.\gradlew assembleDebug

# Install on connected device
.\gradlew installDebug

# Run tests (currently none)
.\gradlew test

# Generate signed release APK
.\gradlew assembleRelease
```

**Debug APK Location:**
```
app\build\outputs\apk\debug\app-debug.apk
```

---

## 🎨 Customization (If Time Permits)

### Change App Name
`app/src/main/res/values/strings.xml`
```xml
<string name="app_name">MyLockerApp</string>
```

### Change Primary Color
`app/src/main/res/values/colors.xml`
```xml
<color name="md_theme_primary">#FF0000</color>
```

### Change TOTP Duration
`app/src/main/java/.../utils/TotpManager.kt`
```kotlin
private const val TIME_STEP = 60L // 60 seconds instead of 30
```

### Change Unlock Duration
`app/src/main/java/.../models/LockedApp.kt`
```kotlin
val tenMinutes = 10 * 60 * 1000L // 10 minutes instead of 5
```

---

## 📚 Key Files to Understand

| File | Purpose | Lines |
|------|---------|-------|
| `AndroidManifest.xml` | App configuration, permissions | ~60 |
| `MainActivity.kt` | App list screen | ~200 |
| `AppBlockerAccessibilityService.kt` | Background monitor | ~60 |
| `BlockerOverlayActivity.kt` | Lock screen | ~150 |
| `TotpActivity.kt` | TOTP manager | ~200 |
| `TotpManager.kt` | TOTP algorithm | ~120 |
| `PreferencesHelper.kt` | Data storage | ~60 |

---

## ⏱️ Time Estimates

| Task | Time |
|------|------|
| First build & run | 5 min |
| Grant permissions | 2 min |
| Test basic locking | 3 min |
| Test TOTP | 5 min |
| Fix build issues | 10 min |
| **Total** | **25 min** |

---

## 🎓 Next Steps After Running

1. ✅ Verify basic locking works
2. ✅ Test TOTP generation
3. ✅ Test TOTP verification
4. 📝 Read ARCHITECTURE.md for design details
5. 📝 Read IMPLEMENTATION.md for API docs
6. 🐛 Fix any bugs you find
7. 🎨 Improve UI if time permits
8. 🚀 Prepare demo presentation

---

## 🏆 Hackathon Tips

- **Demo on emulator** (more reliable than physical device)
- **Pre-lock popular apps** (Instagram, Chrome) before demo
- **Have backup devices** in case of issues
- **Practice the demo 3 times** before presenting
- **Explain the shortcuts** (judges love honesty)
- **Emphasize speed** (built in 24 hours!)

---

## 🆘 Emergency Fixes

### "Everything is broken"
```bash
# Nuclear option - restart from scratch
File > Invalidate Caches > Invalidate and Restart
File > Close Project
Delete .idea/ and .gradle/ folders
Reopen project
```

### "Gradle version mismatch"
Edit `gradle/wrapper/gradle-wrapper.properties`:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-bin.zip
```

### "Compilation errors"
1. Check all files were created correctly
2. Look for red underlines in code
3. Check imports at top of Kotlin files
4. Sync Gradle again

---

**Ready to build? Open Android Studio and click Run!** ▶️

